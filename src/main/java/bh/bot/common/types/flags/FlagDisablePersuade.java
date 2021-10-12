package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.ReRunApp;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.app.farming.RaidApp;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 9)
public class FlagDisablePersuade extends FlagPattern.NonParamFlag {
    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public String getName() {
        return "disable-persuade";
    }

    @Override
    public String getDescription() {
        return "Disable auto persuade & bribe (by default, bot will auto persuade with gold all familiars if your did not setting auto persuade with gold)";
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp
                || instance instanceof ReRunApp
                || instance instanceof RaidApp;
    }
}
