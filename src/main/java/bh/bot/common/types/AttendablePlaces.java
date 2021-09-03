package bh.bot.common.types;

import java.io.IOException;

public class AttendablePlaces {
    public static class Ids {
        // Right
        public static final int Invasion    = 0b00000001;
        public static final int Trials      = 0b00000010;
        public static final int GvG         = 0b00000100;
        public static final int Gauntlet    = 0b00001000;
        public static final int Expedition  = 0b00010000;
        // Left
        public static final int Pvp         = 0b00100000;
        public static final int WorldBoss   = 0b01000000;
        public static final int Raid        = 0b10000000;
    }

    public static AttendablePlace invasion = null;
    public static AttendablePlace expedition = null;
    public static AttendablePlace trials = null;
    public static AttendablePlace gvg = null;
    public static AttendablePlace gauntlet = null;

    public static AttendablePlace pvp = null;
    public static AttendablePlace worldBoss = null;
    public static AttendablePlace raid = null;

    static {
        try {
            invasion = new AttendablePlace("Invasion", AttendablePlaces.Ids.Invasion, "invasion", false);
            expedition = new AttendablePlace("Expedition", AttendablePlaces.Ids.Expedition, "expedition", false);
            trials = new AttendablePlace("Trials", AttendablePlaces.Ids.Trials, "trials", false);
            gvg = new AttendablePlace("GVG", Ids.GvG, "gvg", false);
            gauntlet = new AttendablePlace("Gauntlet", Ids.Gauntlet, "gauntlet", false);

            pvp = new AttendablePlace("PVP", AttendablePlaces.Ids.Pvp, "pvp", true);
            worldBoss = new AttendablePlace("World Boss", AttendablePlaces.Ids.WorldBoss, "world-boss", true, 60);
            raid = new AttendablePlace("Raid", AttendablePlaces.Ids.Raid, "raid", true, 120);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
