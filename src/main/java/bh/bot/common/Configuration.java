package bh.bot.common;

import bh.bot.common.types.ScreenResolutionProfile;
import com.sun.media.sound.InvalidDataException;
import bh.bot.app.AbstractApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import static bh.bot.common.Log.info;
import static bh.bot.common.utils.StringUtil.isNotBlank;

public class Configuration {
    public static class Sizing {
        public static class Fishing {
            public static class Scan {
                public static Size castingFishSize;
            }
        }
    }

    public static class Offsets {
        public static Offset gameScreenOffset;

        public static class Globally {
            public static class Buttons {
                public static Offset reconnectSp;
                public static Offset talkRightArrow;
            }
        }

        public static class Dungeons {
            public static class Buttons {
                public static Offset reRun;
            }
        }

        public static class Fishing {
            public static class Buttons {
                public static Offset start;
                public static Offset cast;
                public static Offset catch_;
            }

            public static class Labels {
                public static Offset fishing;
            }

            public static class Scan {
                public static Offset detectColor100PercentCatchingFish;
                public static Offset beginScanCastingFish;
            }
        }
    }

    public static class Tolerant {
        public static int position;
        public static int color;
        public static int colorBw;
    }

    public static class Game {
        public static int kongUserId;
        public static String kongUserName;
        public static String authToken;
    }

    public static class OS {
        public static final String name = System.getProperty("os.name");
        private static final String normalizedName = name.toLowerCase();
        public static final boolean isMac = normalizedName.indexOf("mac") >= 0 || normalizedName.indexOf("darwin") >= 0;
        public static final boolean isWin = !isMac && normalizedName.indexOf("win") >= 0;
        public static final boolean isUnix = !isMac && !isWin;
    }

    private static Properties properties = new Properties();

    public static void load(ScreenResolutionProfile screenResolutionProfile) throws IOException {
        info(
                "Using '%s' profile which supports %dx%d game resolution",
                screenResolutionProfile.getName(),
                screenResolutionProfile.getSupportedGameResolutionWidth(),
                screenResolutionProfile.getSupportedGameResolutionHeight()
        );

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

        Offsets.gameScreenOffset = Offset.fromKeyPrefix("offset.screen");
        Offsets.Dungeons.Buttons.reRun = screenResolutionProfile.getOffsetButtonDungeonReRun();
        Offsets.Globally.Buttons.talkRightArrow = screenResolutionProfile.getOffsetButtonTalkRightArrow();
        Offsets.Globally.Buttons.reconnectSp = screenResolutionProfile.getOffsetButtonReconnect();
        Offsets.Fishing.Buttons.start = screenResolutionProfile.getOffsetButtonFishingStart();
        Offsets.Fishing.Buttons.cast = screenResolutionProfile.getOffsetButtonFishingCast();
        Offsets.Fishing.Buttons.catch_ = screenResolutionProfile.getOffsetButtonFishingCatch();
        Offsets.Fishing.Labels.fishing = screenResolutionProfile.getOffsetLabelFishing();
        Offsets.Fishing.Scan.detectColor100PercentCatchingFish = screenResolutionProfile.getOffsetDetect100PcCatchingFish();
        Offsets.Fishing.Scan.beginScanCastingFish = screenResolutionProfile.getOffsetScanCastingFish();

        Sizing.Fishing.Scan.castingFishSize = screenResolutionProfile.getScanSizeCastingFish();

        Tolerant.position = Math.max(5, readInt("tolerant.position"));
        Tolerant.color = Math.max(0, readInt("tolerant.color"));
        Tolerant.colorBw = Math.max(0, readInt("tolerant.color.bw"));
    }

    private static final ArrayList<AbstractApplication> applicationInstances = new ArrayList<>();

    public static void registerApplicationInstances(AbstractApplication... instances) {
        for (AbstractApplication instance : instances) {
            applicationInstances.add(instance);
        }
    }

    public static AbstractApplication getInstanceFromAppCode(String code) {
        code = code.toLowerCase().trim();
        for (AbstractApplication applicationInstance : applicationInstances)
            if (code.equals(applicationInstance.getAppCode().toLowerCase().trim()))
                return applicationInstance;
        return null;
    }

    public static String loadGameCfg() {
        try {
            Game.kongUserId = Integer.parseInt(getFromConfigOrEnv("game.kong.user.id", "GAME_BH_KONG_USER_ID"));
            Game.kongUserName = getFromConfigOrEnv("game.kong.user.name", "GAME_BH_KONG_USER_NAME");
            Game.authToken = getFromConfigOrEnv("game.auth.token", "GAME_BH_AUTH_TOKEN");
            if (Game.kongUserId < 1)
                throw new InvalidDataException("Invalid kong user id");
            if (Game.kongUserName == null)
                throw new InvalidDataException("Invalid kong user name");
            if (Game.authToken == null)
                throw new InvalidDataException("Invalid game's auth token");
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
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
