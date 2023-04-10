package bh.bot.common.types;

import java.io.IOException;

public class AttendablePlaces {
    public static class Ids {
        // Right
        public static final long Invasion    = 0b000000001;
        public static final long Fishing     = 0b000000011;
        public static final long Trials      = 0b000000010;
        public static final long GvG         = 0b000000100;
        public static final long Gauntlet    = 0b000001000;
        public static final long Expedition  = 0b000010000;
        // Left
        public static final long Pvp         = 0b000100000;
        public static final long WorldBoss   = 0b001000000;
        public static final long Raid        = 0b010000000;
        public static final long Quest       = 0b100000000;
    }

    public static AttendablePlace invasion = null;
    public static AttendablePlace fishing = null;
    public static AttendablePlace expedition = null;
    public static AttendablePlace trials = null;
    public static AttendablePlace gvg = null;
    public static AttendablePlace gauntlet = null;

    public static AttendablePlace pvp = null;
    public static AttendablePlace worldBoss = null;
    public static AttendablePlace raid = null;
    public static AttendablePlace quest = null;


    static {
        try {
            invasion = new AttendablePlace("Invasion", AttendablePlaces.Ids.Invasion, "invasion", false);
            fishing = new AttendablePlace("Claim Fishing Bait", AttendablePlaces.Ids.Fishing,  "bait", false, 60*24);
            expedition = new AttendablePlace("Expedition", AttendablePlaces.Ids.Expedition, "expedition", false);
            trials = new AttendablePlace("Trials", AttendablePlaces.Ids.Trials, "trials", false);
            gvg = new AttendablePlace("GVG", Ids.GvG, "gvg", false);
            gauntlet = new AttendablePlace("Gauntlet", Ids.Gauntlet, "gauntlet", false);

            pvp = new AttendablePlace("PVP", AttendablePlaces.Ids.Pvp, "pvp", true);
            worldBoss = new AttendablePlace("World Boss", AttendablePlaces.Ids.WorldBoss, "world-boss", true, 60);
            raid = new AttendablePlace("Raid", AttendablePlaces.Ids.Raid, "raid", true, 120);
            quest = new AttendablePlace("Quest", AttendablePlaces.Ids.Quest,  "quest", true, 120);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
