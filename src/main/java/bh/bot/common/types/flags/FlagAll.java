package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagAll extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "all";
    }

    @Override
    public String getDescription() {
        return "Include all local options (except --exit)";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public <TApp extends AbstractApplication> boolean isSupportedByApp(TApp instance) {
        return instance instanceof AfkApp;
    }
}
