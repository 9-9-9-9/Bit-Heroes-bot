package bh.bot.common.types;

import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.tuples.Tuple2;

public class UserConfig {
    public static final String raidLevelKey = "ig.user.raid.level";
    public static final String raidModeKey = "ig.user.raid.mode";
    public static final String worldBossLevelKey = "ig.user.world-boss.level";

    public static final byte modeNormal = 1;
    public static final byte modeHard = 2;
    public static final byte modeHeroic = 3;

    private static final byte raidLevelMin = 1;
    private static final byte raidLevelMax = 13;
    private static final byte worldBossLevelMin = 1;
    private static final byte worldBossLevelMax = 8;

    public final String cfgProfileName;
    public final byte raidLevel;
    public final byte raidMode;
    public final byte worldBossLevel;

    public UserConfig(String cfgProfileName, byte raidLevel, byte raidMode, byte worldBossLevel) {
        this.cfgProfileName = cfgProfileName;
        this.raidLevel = raidLevel;
        this.raidMode = raidMode;
        this.worldBossLevel = worldBossLevel;
    }

    public String getRaidLevelDesc() {
        if (!isValidRaidLevel())
            throw new InvalidDataException(
                    "Invalid Raid level %d. Must in range %d to %d. Level 1 stands for R1 (T4) and level %d stands for R%d (T%d)",
                    raidLevel, raidLevelMin, raidLevelMax, raidLevelMax, raidLevelMax, raidLevelMax + 3);
        return getRaidLevelDesc(raidLevel);
    }

    public String getWorldBossLevelDesc() {
        if (!isValidWorldBossLevel())
            throw new InvalidDataException("Invalid World Boss level %d. Must in range %d to %d", worldBossLevel,
                    worldBossLevelMin, worldBossLevelMax);
        return getWorldBossLevelDesc(worldBossLevel);
    }

    public String getRaidModeDesc() {
        return getDifficultyModeDesc(raidMode, "Raid");
    }

    public boolean isValidRaidLevel() {
        return raidLevel >= raidLevelMin && raidLevel <= raidLevelMax;
    }

    public boolean isValidWorldBossLevel() {
        return worldBossLevel >= worldBossLevelMin && worldBossLevel <= worldBossLevelMax;
    }

    public static Tuple2<Byte, Byte> getRaidLevelRange() {
        return new Tuple2<>(raidLevelMin, raidLevelMax);
    }

    public static Tuple2<Byte, Byte> getWorldBossLevelRange() {
        return new Tuple2<>(worldBossLevelMin, worldBossLevelMax);
    }

    public static Tuple2<Byte, Byte> getModeRange() {
        return new Tuple2<>(modeNormal, modeHeroic);
    }

    public static String getDifficultyModeDesc(byte mode, String name) {
        if (!isValidDifficultyMode(mode))
            return "Not specified";

        switch (mode) {
            case modeNormal:
                return "NORMAL";
            case modeHard:
                return "HARD";
            case modeHeroic:
                return "HEROIC";
            default:
                throw new InvalidDataException("Invalid %s mode %d", name, mode);
        }
    }

    public static boolean isValidDifficultyMode(byte mode) {
        switch (mode) {
            case modeNormal:
            case modeHard:
            case modeHeroic:
                return true;
            default:
                return false;
        }
    }

    public static boolean isNormalMode(byte mode) {
        return mode == modeNormal;
    }

    public static boolean isHardMode(byte mode) {
        return mode == modeHard;
    }

    public static boolean isHeroicMode(byte mode) {
        return mode == modeHeroic;
    }

    public static String getRaidLevelDesc(int level) {
        return String.format("R%d (T%d)", level, level + 3);
    }

    public static String getWorldBossLevelDesc(int level) {
        switch (level) {
            case 1:
                return "Orlag Clan (T3-T12)";
            case 2:
                return "Netherworld (T3-T13)";
            case 3:
                return "Melvin Factory (T10-T11)";
            case 4:
                return "3XT3RM1N4T10N (T10-T11)";
            case 5:
                return "Brimstone Syndicate (T11-T12)";
            case 6:
                return "Titans Attack! (T11-T16)";
            case 7:
                return "The Ignited Abyss";
            case 8:
                return "The Wolf's Deception (T13-T16)";
            default:
                return "Unknown (T?-T?)";
        }
    }
}
