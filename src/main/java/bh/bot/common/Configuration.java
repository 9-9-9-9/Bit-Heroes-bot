package bh.bot.common;

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
        }
    }

    public static class Tolerant {
        public static int position;
        public static int color;
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

    public static void load() throws IOException {
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
        Offsets.Dungeons.Buttons.reRun = Offset.fromKeyPrefix("offset.dungeons.buttons.rerun-kp");
        Offsets.Globally.Buttons.talkRightArrow = Offset.fromKeyPrefix("offset.globally.buttons.talkRightArrow");
        Offsets.Globally.Buttons.reconnectSp = Offset.fromKeyPrefix("offset.globally.buttons.reconnect-sp");
        Offsets.Fishing.Buttons.start = Offset.fromKeyPrefix("offset.fishing.buttons.start-sp");
        Offsets.Fishing.Buttons.cast = Offset.fromKeyPrefix("offset.fishing.buttons.cast-sp");
        Offsets.Fishing.Buttons.catch_ = Offset.fromKeyPrefix("offset.fishing.buttons.catch-sp");
        Offsets.Fishing.Labels.fishing = Offset.fromKeyPrefix("offset.fishing.labels.fishing-mx");

        Tolerant.position = Math.max(5, readInt("tolerant.position"));
        Tolerant.color = Math.max(0, readInt("tolerant.color"));
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
        public int X;
        public int Y;

        private Offset(int x, int y) {
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
}
