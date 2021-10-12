package bh.bot.common;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.StringUtil.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.AtomicOffset;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.flags.FlagAlterLoopInterval;
import bh.bot.common.types.flags.FlagProfileName;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.StringUtil;
import bh.bot.common.utils.TimeUtil;
import bh.bot.common.utils.ValidationUtil;

public class Configuration {
    public static ScreenResolutionProfile screenResolutionProfile = null;
    public static String profileName = null;
    public static boolean isSteamProfile = false;
    public static boolean isWebProfile = false;
    public static AtomicOffset gameScreenOffset;
    public static final boolean enableDevFeatures = new File("im.dev").exists();
    public static boolean noThrowWhenImageNotAvailable = false;

    public static class Interval {

        public static class Loop {
            public static int main = -1;

            public static int getMainLoopInterval(int defaultIntervalValue) {
                if (main < 1)
                    return defaultIntervalValue;
                return main;
            }
        }
    }

    public static class Features {
        public static boolean disableJna = false;
        public static boolean disableDoCheckGameScreenOffset = false;
        public static boolean disableColorizeTerminal = false;
        public static boolean disableAutoUpdate = false;
    }

    public static class Tolerant {
        public static final byte minPositionTolerant = 5;
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

        Configuration.screenResolutionProfile = screenResolutionProfile;
        isSteamProfile = parseArgumentsResult.steam;
        isWebProfile = parseArgumentsResult.web;

        profileName = screenResolutionProfile.getName().trim();
        if (isBlank(profileName))
            throw new InvalidDataException("profileName");

        if (isSteamProfile)
            warn(
                    "Your Bit Heroes game window resolution will be automatically adjusted to %dx%d",
                    screenResolutionProfile.getSupportedGameResolutionWidth(),
                    screenResolutionProfile.getSupportedGameResolutionHeight()
            );
        else
            warn("You must move the Bit Heroes game's window to top left corner of your screen or provide exactly screen offset into the 'offset.screen.x & y' keys. See more: https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Manually-setting-game-screen-coordinate");

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

        if (parseArgumentsResult.mainLoopInterval >= FlagAlterLoopInterval.minimumValue) {
            Interval.Loop.main = parseArgumentsResult.mainLoopInterval;
        } else {
            Interval.Loop.main = readIntervalConfig("interval.loop.main");
        }

        Features.disableJna = StringUtil.isTrue(read("disable.jna"));
        Features.disableDoCheckGameScreenOffset =
                Features.disableJna || StringUtil.isTrue(read("disable.jna.disableDoCheckGameScreenOffset"));
        Features.disableColorizeTerminal = StringUtil.isTrue(read("disable.jansi")) || Main.forceDisableAnsi;
        Features.disableAutoUpdate = StringUtil.isTrue(read("disable.auto-update"));

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

        Tolerant.position = Math.max(Tolerant.minPositionTolerant, readInt("tolerant.position"));
        Tolerant.color = Math.max(0, readInt("tolerant.color"));
        Tolerant.colorBw = (byte) Math.max(0, readInt("tolerant.color.bw"));
        Tolerant.colorBwL2 = (byte) Math.max(0, readInt("tolerant.color.bw.l2"));
        debug("Tolerant.color     = %d", Tolerant.color);
        debug("Tolerant.colorBw   = %d", Tolerant.colorBw);
        debug("Tolerant.colorBwL2 = %d", Tolerant.colorBwL2);
    }

    public static Tuple2<Boolean, UserConfig> loadUserConfig(String cfgProfileName) throws IOException { // returns tuple of
        // File Exists + Data

        if (!ValidationUtil.isValidUserProfileName(cfgProfileName)) {
            err("That's not a valid profile name, correct format should be: %s", FlagProfileName.formatDesc);
            return new Tuple2<>(false, null);
        }

        String profileConfigFileName = getProfileConfigFileName(cfgProfileName);
        final File fileCfg = new File(profileConfigFileName);
        if (!fileCfg.exists() || !fileCfg.isFile()) {
            debug("Unable to load user config for profile '%s', reason: file '%s' not found", cfgProfileName,
                    profileConfigFileName);
            return new Tuple2<>(false, null);
        }

        byte raidLevel, raidMode, worldBossLevel, expeditionPlace, pvpTarget;

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

        try {
            expeditionPlace = Byte.parseByte(readKey(properties, UserConfig.expeditionPlaceKey, "0", "Not specified"));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", UserConfig.expeditionPlaceKey);
        }

        try {
            pvpTarget = Byte.parseByte(readKey(properties, UserConfig.pvpTargetKey, "1", "Not specified"));
        } catch (NumberFormatException ex) {
            throw new InvalidDataException("Value of key '%s' is not a number", UserConfig.pvpTargetKey);
        }

        return new Tuple2<>(true, new UserConfig(cfgProfileName, raidLevel, raidMode, worldBossLevel, expeditionPlace, pvpTarget));
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

    public static String getProfileConfigFileName(String cfgProfileName) {
        return String.format("readonly.%s.user-config.properties", cfgProfileName);
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

    public static int readIntervalConfig(String key) {
        String val = read(key);
        if (StringUtil.isBlank(val))
            return FlagAlterLoopInterval.defaultValue;

        Tuple3<Boolean, String, Integer> tuple3 = TimeUtil.tryParseTimeConfig(val, FlagAlterLoopInterval.defaultValue);
        if (tuple3._1) {
            final int interval = tuple3._3;
            if (interval < FlagAlterLoopInterval.minimumValue) {
                err("Minimum value of key '%s' is 50ms this value '%s' will be ignored", key, val);
                return FlagAlterLoopInterval.defaultValue;
            }

            if (interval > FlagAlterLoopInterval.maximumValue) {
                err("Maximum value of key '%s' is %ds (%dms) thus value '%s' will be ignored", key, FlagAlterLoopInterval.maximumValue / 1_000, FlagAlterLoopInterval.maximumValue, val);
                return FlagAlterLoopInterval.defaultValue;
            }

            if (interval % 1_000 == 0)
                warn("Main loop interval was modified to %ds", interval / 1_000);
            else if (interval > 1_000)
                warn("Main loop interval was modified to %dms (~%d seconds)", interval, interval /  1_000);
            else
                warn("Main loop interval was modified to %dms", interval);

            return interval;
        }

        err("Failed to parse value '%s' of key '%s'! Reason: %s", val, key, tuple3._2);
        return FlagAlterLoopInterval.defaultValue;
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
