package bh.bot.common;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.StringUtil.isNotBlank;

public class Configuration {
    public static ScreenResolutionProfile screenResolutionProfile = null;
    public static String profileName = null;
    public static Offset gameScreenOffset;
    public static final boolean enableDevFeatures = new File("im.dev").exists();

    public static class Tolerant {
        public static int position;
        public static int color;
        public static byte colorBw;
    }

    public static class OS {
        public static final String name = System.getProperty("os.name");
        private static final String normalizedName = name.toLowerCase();
        public static final boolean isMac = normalizedName.indexOf("mac") >= 0 || normalizedName.indexOf("darwin") >= 0;
        public static final boolean isWin = !isMac && normalizedName.indexOf("win") >= 0;
        public static final boolean isUnix = !isMac && !isWin;
    }

    public static class UserConfig {
        public static int profileNo;
        public static byte raidLevel;
        public static byte raidMode;

        public static final byte modeNormal = 1;
        public static final byte modeHard = 2;
        public static final byte modeHeroic = 3;

        public static String getRaidModeDesc(byte mode) {
            switch (mode) {
                case modeNormal:
                    return "NORMAL";
                case modeHard:
                    return "HARD";
                case modeHeroic:
                    return "HEROIC";
                default:
                    return "Not specified";
            }
        }
    }

    private static Properties properties = new Properties();

    public static void loadSystemConfig(ScreenResolutionProfile screenResolutionProfile) throws IOException {
        info(
                "Using '%s' profile which supports %dx%d game resolution",
                screenResolutionProfile.getName(),
                screenResolutionProfile.getSupportedGameResolutionWidth(),
                screenResolutionProfile.getSupportedGameResolutionHeight()
        );

        Configuration.screenResolutionProfile = screenResolutionProfile;
        Configuration.profileName = screenResolutionProfile.getName().trim();
        if (isBlank(profileName))
            throw new InvalidDataException("profileName");

        if (screenResolutionProfile instanceof ScreenResolutionProfile.SteamProfile) {
            if (!OS.isWin) {
                err("Steam profile only available on Windows");
                System.exit(Main.EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
            }
            info("****** IMPORTANT ****** IMPORTANT ******");
            warn("You must move the Bit Heroes game's window to top left corner of your screen or provide exactly screen offset into the 'offset.screen.x & y' keys");
            warn("Your Bit Heroes game window must be 800x480. You can check it by open Settings, see the Windowed option");
            info("****************************************");
        }

        properties.load(Configuration.class.getResourceAsStream("/config.properties"));

        File cfgOverride = new File("user-config.properties");
        if (cfgOverride.exists() && cfgOverride.isFile()) {
            info("Going to load configuration from %s", cfgOverride.getName());
            try (InputStream inputStream = new FileInputStream(cfgOverride)) {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        gameScreenOffset = Offset.fromKeyPrefix("offset.screen");

        Tolerant.position = Math.max(5, readInt("tolerant.position"));
        Tolerant.color = Math.max(0, readInt("tolerant.color"));
        Tolerant.colorBw = (byte) Math.max(0, readInt("tolerant.color.bw"));
    }

    public static void loadUserConfig(int profileNo) throws IOException {
        if (profileNo < 1)
            return;
        String profileConfigFileName = getProfileConfigFileName(profileNo);
        final File fileCfg = new File(profileConfigFileName);
        if (!fileCfg.exists() || !fileCfg.isFile()) {
            debug("Unable to load user config for profile no.%d, reason: file '%s' not found", profileNo, profileConfigFileName);
            return;
        }

        if (profileNo > 1)
            info("Going to load configuration from %s", fileCfg.getName());

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(fileCfg)) {
            properties.load(inputStream);
        }

        UserConfig.profileNo = profileNo;

        String raidLevelKey = "ig.user.raid.level";
        String raidLevel = readKey(properties, raidLevelKey, "0", "Not specified");
        try {
            UserConfig.raidLevel = Byte.parseByte(raidLevel);
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", raidLevelKey);
        }

        String raidModeKey = "ig.user.raid.mode";
        String raidMode = readKey(properties, raidModeKey, "0", "Not specified");
        try {
            UserConfig.raidMode = Byte.parseByte(raidMode);
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", raidModeKey);
        }

        info("Profile %d has configured raid level = %d", profileNo, UserConfig.raidLevel);
        info("Profile %d has configured raid mode = %d (%s)", profileNo, UserConfig.raidMode, UserConfig.getRaidModeDesc(UserConfig.raidMode));
    }

    private static String readKey(Properties properties, String key, String defaultValue, String defaultValueDesc) {
        String value = properties.getProperty(key);
        if (isBlank(value)) {
            info("Key '%s' does not exists, default value '%s' will be used (%s)", key, defaultValue, defaultValueDesc);
            return defaultValue;
        }
        return value;
    }

    public static String getProfileConfigFileName(int profileNo) {
        return String.format("readonly.%d.user-config.properties", profileNo);
    }

    private static final ArrayList<Tuple2<Class<? extends AbstractApplication>, String>> applicationClassesInfo = new ArrayList<>();

    public static void registerApplicationInstances(Class<? extends AbstractApplication>... classes) {
        for (Class<? extends AbstractApplication> class_ : classes) {
            AppCode annotation = class_.getAnnotation(AppCode.class);
            if (annotation == null)
                throw new NotImplementedException(String.format("App '%s' missing @%s annotation", class_.getSimpleName(), AppCode.class.getSimpleName()));
            String appCode = annotation.code();
            if (StringUtil.isBlank(appCode))
                throw new NotImplementedException(String.format("App '%s' missing app code in @%s annotation", class_.getSimpleName(), AppCode.class.getSimpleName()));
            if (!appCode.equals(appCode.trim().toLowerCase()))
                throw new RuntimeException(String.format("App code of app '%s' has to be normalized", class_.getSimpleName()));
            applicationClassesInfo.add(new Tuple2<>(class_, appCode));
        }
    }

    public static Class<? extends AbstractApplication> getApplicationClassFromAppCode(String code) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        code = code.toLowerCase().trim();
        for (Tuple2<Class<? extends AbstractApplication>, String> applicationClassInfo : applicationClassesInfo) {
            if (!code.equals(applicationClassInfo._2))
                continue;
            return applicationClassInfo._1;
        }
        err("Not match any app code");
        return null;
    }

    public static String read(String key) {
        return properties.getProperty(key);
    }

    public static int readInt(String key) {
        return Integer.parseInt(read(key));
    }

    public static String getFromConfigOrEnv(String cfgKey, String envKey) {
        String cfg = Configuration.read(cfgKey);
        if (isNotBlank(cfg))
            return cfg.trim();

        String env = System.getenv(envKey);
        if (isNotBlank(env))
            return env.trim();

        return null;
    }

    public static class Offset {
        public final int X;
        public final int Y;

        public Offset(int x, int y) {
            this.X = x;
            this.Y = y;
        }

        public static Offset fromKeyPrefix(String keyPrefix) {
            int x = Configuration.readInt(keyPrefix + ".x");
            int y = Configuration.readInt(keyPrefix + ".y");
            if (x < 0)
                throw new IllegalArgumentException(String.format("Value of offset %s.x can not be a negative number: %d", keyPrefix, x));
            if (y < 0)
                throw new IllegalArgumentException(String.format("Value of offset %s.y can not be a negative number: %d", keyPrefix, y));
            return new Offset(x, y);
        }

        public static Offset none() {
            return new Offset(-1, -1);
        }
    }

    public static class Size {
        public final int W;
        public final int H;

        public Size(int w, int h) {
            this.W = w;
            this.H = h;
        }
    }
}
