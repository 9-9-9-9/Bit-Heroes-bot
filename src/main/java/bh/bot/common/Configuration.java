package bh.bot.common;

import bh.bot.app.AbstractApplication;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.*;
import bh.bot.common.types.ScreenResolutionProfile.SteamProfile;
import bh.bot.common.types.ScreenResolutionProfile.WebProfile;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.StringUtil.isNotBlank;

public class Configuration {
    public static ScreenResolutionProfile screenResolutionProfile = null;
    public static String profileName = null;
    public static boolean isSteamProfile = false;
    public static boolean isWebProfile = false;
    public static AtomicOffset gameScreenOffset;
    public static final boolean enableDevFeatures = new File("im.dev").exists();
    public static boolean noThrowWhenImageNotAvailable = false;

    public static class Features {
        public static boolean disableJna = false;
        public static boolean disableDoCheckGameScreenOffset = false;
        public static boolean disableColorizeTerminal = "windows 7".equals(OS.name.trim().toLowerCase());
    }

    public static class Tolerant {
        public static int position;
        public static int color;
        public static byte colorBw;
        public static byte colorBwL2;
    }

    public static class Timeout {
        public static short longTimeNoSeeInMinutes = 0;
        public static final byte defaultLongTimeNoSeeInMinutes = 15;
    }

    private static Properties properties = new Properties();

    public static void loadSystemConfig(final ParseArgumentsResult parseArgumentsResult) throws IOException {
        final ScreenResolutionProfile screenResolutionProfile = parseArgumentsResult.screenResolutionProfile;
        info("Using '%s' profile which supports %dx%d game resolution", screenResolutionProfile.getName(),
                screenResolutionProfile.getSupportedGameResolutionWidth(),
                screenResolutionProfile.getSupportedGameResolutionHeight());

        Configuration.screenResolutionProfile = screenResolutionProfile;
        isSteamProfile = screenResolutionProfile instanceof SteamProfile;
        isWebProfile = screenResolutionProfile instanceof WebProfile;
        profileName = screenResolutionProfile.getName().trim();
        if (isBlank(profileName))
            throw new InvalidDataException("profileName");

        warn("You must move the Bit Heroes game's window to top left corner of your screen or provide exactly screen offset into the 'offset.screen.x & y' keys");
        if (isSteamProfile)
            warn("Your Bit Heroes game window must be 800x480. You can check it by open Settings, see the Windowed option");

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

        noThrowWhenImageNotAvailable = StringUtil.isTrue(read("dev.no-throw-when-image-not-available"));
        Features.disableJna = StringUtil.isTrue(read("disable.jna"));
        Features.disableDoCheckGameScreenOffset =
                Features.disableJna || StringUtil.isTrue(read("disable.jna.disableDoCheckGameScreenOffset"));
        Features.disableColorizeTerminal = StringUtil.isTrue(read("disable.colorize-terminal"));

        String keyLongTimeNoSee = "timeout.minutes.long-time-no-see";
        short minLongTimeNoSee = 10;
        try {
            Timeout.longTimeNoSeeInMinutes = (short) Math.max(minLongTimeNoSee, readInt(keyLongTimeNoSee));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException(
                    "Value of key '%s' must be a number, minimum value accepted is %d, default value is %d",
                    keyLongTimeNoSee, minLongTimeNoSee, Timeout.defaultLongTimeNoSeeInMinutes);
        }

        gameScreenOffset = new AtomicOffset(Offset.fromKeyPrefix("offset.screen"));

        Tolerant.position = Math.max(5, readInt("tolerant.position"));
        Tolerant.color = Math.max(0, readInt("tolerant.color"));
        Tolerant.colorBw = (byte) Math.max(0, readInt("tolerant.color.bw"));
        Tolerant.colorBwL2 = (byte) Math.max(0, readInt("tolerant.color.bw.l2"));
        debug("Tolerant.color     = %d", Tolerant.color);
        debug("Tolerant.colorBw   = %d", Tolerant.colorBw);
        debug("Tolerant.colorBwL2 = %d", Tolerant.colorBwL2);
    }

    public static Tuple2<Boolean, UserConfig> loadUserConfig(int profileNo) throws IOException { // returns tuple of
        // File Exists +
        // Data
        String profileConfigFileName = getProfileConfigFileName(profileNo);
        final File fileCfg = new File(profileConfigFileName);
        if (!fileCfg.exists() || !fileCfg.isFile()) {
            debug("Unable to load user config for profile no.%d, reason: file '%s' not found", profileNo,
                    profileConfigFileName);
            return new Tuple2<>(false, null);
        }

        byte raidLevel, raidMode, worldBossLevel;

        if (profileNo > 1)
            info("Going to load configuration from %s", fileCfg.getName());

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(fileCfg)) {
            properties.load(inputStream);
        }

        try {
            raidLevel = Byte.parseByte(readKey(properties, UserConfig.raidLevelKey, "0", "Not specified"));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", UserConfig.raidLevelKey);
        }

        try {
            raidMode = Byte.parseByte(readKey(properties, UserConfig.raidModeKey, "0", "Not specified"));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", UserConfig.raidModeKey);
        }

        try {
            worldBossLevel = Byte.parseByte(readKey(properties, UserConfig.worldBossLevelKey, "0", "Not specified"));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", UserConfig.worldBossLevelKey);
        }

        return new Tuple2<>(true, new UserConfig(profileNo, raidLevel, raidMode, worldBossLevel));
    }

    @SuppressWarnings("SameParameterValue")
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

    private static final ArrayList<Tuple2<Class<? extends AbstractApplication>, AppMeta>> applicationClassesInfo = new ArrayList<>();

    @SafeVarargs
    public static void registerApplicationClasses(Class<? extends AbstractApplication>... classes) {
        for (Class<? extends AbstractApplication> class_ : classes) {
            AppMeta annotation = class_.getAnnotation(AppMeta.class);
            if (annotation == null)
                throw new NotImplementedException(String.format("App '%s' missing @%s annotation",
                        class_.getSimpleName(), AppMeta.class.getSimpleName()));
            String appCode = annotation.code();
            if (StringUtil.isBlank(appCode))
                throw new NotImplementedException(String.format("App '%s' missing app code in @%s annotation",
                        class_.getSimpleName(), AppMeta.class.getSimpleName()));
            if (!appCode.equals(appCode.trim().toLowerCase()))
                throw new RuntimeException(
                        String.format("App code of app '%s' has to be normalized", class_.getSimpleName()));
            if (StringUtil.isBlank(annotation.name()))
                throw new NotImplementedException(String.format("App '%s' missing name in @%s annotation",
                        class_.getSimpleName(), AppMeta.class.getSimpleName()));
            applicationClassesInfo.add(new Tuple2<>(class_, annotation));
        }
    }

    public static Class<? extends AbstractApplication> getApplicationClassFromAppCode(String code) {
        code = code.toLowerCase().trim();
        for (Tuple2<Class<? extends AbstractApplication>, AppMeta> applicationClassInfo : applicationClassesInfo) {
            if (!code.equals(applicationClassInfo._2.code()))
                continue;
            return applicationClassInfo._1;
        }
        err("Not match any app code");
        return null;
    }

    public static java.util.List<Tuple2<Class<? extends AbstractApplication>, AppMeta>> getApplicationClasses(boolean includeDevApps) {
        return applicationClassesInfo.stream()
                .filter(x -> includeDevApps || !x._2.dev())
                .sorted(Comparator.comparingDouble((Tuple2<Class<? extends AbstractApplication>, AppMeta> o) -> o._2.displayOrder()).thenComparing(o -> o._2.name()))
                .collect(Collectors.toList());
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
}
