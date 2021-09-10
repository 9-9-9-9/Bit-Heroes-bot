package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotSupportedException;

public class FlagDoAfk extends FlagPattern<FlagDoAfk.AfkBatch> {
    public static final char codePvp = 'P';
    public static final char codeWorldBoss1 = 'B';
    public static final char codeWorldBoss2 = 'W';
    public static final char codeRaid = 'R';
    public static final char codeInvasion = 'I';
    public static final char codeExpedition = 'E';
    public static final char codeGVG = 'V';
    public static final char codeTrials = 'T';
    public static final char codeGauntlet = 'G';
    public static final char codeComboInvasionGvgExpedition = '1';
    public static final char codeComboTrialsGauntlet = '2';
    public static final char codeComboPvpWorldBossRaid = '3';
    public static final char codeComboAll = 'A';

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public String getName() {
        return "afk";
    }

    @Override
    public String getDescription() {
        return String.format("Short way to include tasks to do by AFK. Accepted values are combination of: %s", shortDesc);
    }

    private final String shortDesc = String.format("%s (PVP), %s (World Boss), %s (Raid), %s (Invasion), %s (Expedition), %s (GVG), %s (Gauntlet), %s (Trials), %s (Invasion/GVG/Expedition), %s (Trials/Gauntlet), %s (PVP/World Boss/Raid), %s (All)", codePvp, codeWorldBoss1, codeRaid, codeInvasion, codeExpedition, codeGVG, codeGauntlet, codeTrials, codeComboInvasionGvgExpedition, codeComboTrialsGauntlet, codeComboPvpWorldBossRaid, codeComboAll);

    @Override
    public boolean isAllowParam() {
        return true;
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }

    @Override
    protected AfkBatch internalParseParam(String paramPart) throws InvalidFlagException {
        AfkBatch result = new AfkBatch();
        for (char c : paramPart.toUpperCase().trim().toCharArray()) {
            if (c == codeComboAll) {
                result.doPvp = true;
                result.doWorldBoss = true;
                result.doRaid = true;
                result.doInvasion = true;
                result.doExpedition = true;
                result.doGvg = true;
                result.doTrials = true;
                result.doGauntlet = true;
            } else if (c == codeComboPvpWorldBossRaid) {
                result.doPvp = true;
                result.doWorldBoss = true;
                result.doRaid = true;
            } else if (c == codeComboInvasionGvgExpedition) {
                result.doInvasion = true;
                result.doExpedition = true;
                result.doGvg = true;
            } else if (c == codeComboTrialsGauntlet) {
                result.doTrials = true;
                result.doGauntlet = true;
            } else if (c == codePvp) {
                result.doPvp = true;
            } else if (c == codeWorldBoss1 || c == codeWorldBoss2) {
                result.doWorldBoss = true;
            } else if (c == codeRaid) {
                result.doRaid = true;
            } else if (c == codeInvasion) {
                result.doInvasion = true;
            } else if (c == codeGVG) {
                result.doGvg = true;
            } else if (c == codeExpedition) {
                result.doExpedition = true;
            } else if (c == codeTrials) {
                result.doTrials = true;
            } else if (c == codeGauntlet) {
                result.doGauntlet = true;
            } else if (c == ',' || c == ';' || c == '+') {
                continue;
            } else {
                throw new InvalidFlagException(String.format("Unrecognized code value '%s' for flag '%s'. Accepted values are: %s", c, getCode(), shortDesc));
            }
        }

        return result;
    }

    public static class AfkBatch {
        public boolean doPvp;
        public boolean doWorldBoss;
        public boolean doRaid;
        public boolean doInvasion;
        public boolean doExpedition;
        public boolean doGvg;
        public boolean doTrials;
        public boolean doGauntlet;
    }
}
