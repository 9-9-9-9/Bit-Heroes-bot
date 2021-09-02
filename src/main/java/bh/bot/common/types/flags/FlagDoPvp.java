package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagDoPvp extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "pvp";
    }

    @Override
    public String getDescription() {
        return "Auto doing PVP";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}
