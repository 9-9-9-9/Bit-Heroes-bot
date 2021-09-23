package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.farming.ExpeditionApp;
import bh.bot.app.farming.RaidApp;
import bh.bot.app.farming.WorldBossApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.types.annotations.FlagMeta;
import bh.bot.common.utils.ValidationUtil;

@FlagMeta(cbVal = "1", checked = true, displayOrder = 2)
public class FlagProfileName extends FlagPattern<String> {
    public static final String formatDesc = "a-z 0-9 and can contains '-' or '_' character";

    @Override
    protected String internalParseParam(String paramPart) throws InvalidFlagException {
        String cfgProfileName = paramPart.trim().toLowerCase();
        if (!ValidationUtil.isValidUserProfileName(cfgProfileName))
            throw new InvalidFlagException(String.format("Value of flag '%s' is malformed, valid format should be: %s", getCode(), formatDesc));
        return cfgProfileName;
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "Specific profile to load. Used when having multiple accounts configured, default value is 1";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp
                || instance instanceof WorldBossApp
                || instance instanceof RaidApp
                || instance instanceof ExpeditionApp;
    }

    @Override
    public boolean isAllowParam() {
        return true;
    }
}