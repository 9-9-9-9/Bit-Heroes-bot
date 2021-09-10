package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.utils.StringUtil;

public class FlagDoAfk extends FlagPattern<FlagDoAfk.AfkBatch> {
    private static final char codePvp = 'P';
    private static final char codeWorldBoss1 = 'B';
    private static final char codeWorldBoss2 = 'W';
    private static final char codeRaid = 'R';
    private static final char codeInvasion = 'I';
    private static final char codeExpedition = 'E';
    private static final char codeGVG = 'V';
    private static final char codeTrials = 'T';
    private static final char codeGauntlet = 'G';
    private static final char codeComboPvpWorldBossRaid = '1';
    private static final char codeComboInvasionGvgExpedition = '2';
    private static final char codeComboTrialsGauntlet = '3';
    private static final char codeComboAll = 'A';

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
        return String.format("Short way to include tasks to do by AFK. Accepted values are combination of: %s. For example: `%s=%s%s` means you want to do PVP and GVG or `%s=%s` means you want to do everything", shortDesc, getCode(), codePvp, codeGVG, getCode(), codeComboAll);
    }

    private final String shortDesc = String.format("%s (PVP), %s (World Boss), %s (Raid), %s (Invasion), %s (Expedition), %s (GVG), %s (Gauntlet), %s (Trials), %s (PVP/World Boss/Raid), %s (Invasion/GVG/Expedition), %s (Trials/Gauntlet), %s (All)", codePvp, codeWorldBoss1, codeRaid, codeInvasion, codeExpedition, codeGVG, codeGauntlet, codeTrials, codeComboPvpWorldBossRaid, codeComboInvasionGvgExpedition, codeComboTrialsGauntlet, codeComboAll);

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
        final String normalized = paramPart.toUpperCase().trim();
        final AfkBatch result = new AfkBatch();

        if (StringUtil.isBlank(normalized))
            throw new InvalidFlagException(String.format("Flag `%s` requires parameter. Valid values are combination of: %s", getCode(), shortDesc));

        for (char c : normalized.toCharArray()) {
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
