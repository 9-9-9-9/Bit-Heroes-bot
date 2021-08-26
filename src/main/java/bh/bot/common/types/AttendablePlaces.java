package bh.bot.common.types;

import java.io.IOException;

public class AttendablePlaces {
    public static class Ids {
        // Right
        public static final int Invasion = 1;
        public static final int Trials = 2;
        // Left
        public static final int Pvp = 11;
        public static final int WorldBoss = 12;
        public static final int Raid = 13;
    }

    public static AttendablePlace invasion = null;
    public static AttendablePlace trials = null;

    public static AttendablePlace pvp = null;
    public static AttendablePlace worldBoss = null;
    public static AttendablePlace raid = null;

    static {
        try {
            invasion = new AttendablePlace("Invasion", AttendablePlaces.Ids.Invasion, "invasion-mx.bmp", false);
            trials = new AttendablePlace("Trials", AttendablePlaces.Ids.Trials, "trials-mx.bmp", false);

            pvp = new AttendablePlace("PVP", AttendablePlaces.Ids.Pvp, "pvp-mx.bmp", true);
            worldBoss = new AttendablePlace("World Boss", AttendablePlaces.Ids.WorldBoss, "world-boss-mx.bmp", true);
            raid = new AttendablePlace("Raid", AttendablePlaces.Ids.Raid, "raid-mx.bmp", true, 120);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
