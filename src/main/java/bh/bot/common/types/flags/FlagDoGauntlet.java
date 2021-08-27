package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagDoGauntlet extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "gauntlet";
    }

    @Override
    public String getDescription() {
        return "Auto doing Gauntlet";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public <TApp extends AbstractApplication> boolean internalCheckIsSupportedByApp(TApp instance) {
        return instance instanceof AfkApp;
    }
}