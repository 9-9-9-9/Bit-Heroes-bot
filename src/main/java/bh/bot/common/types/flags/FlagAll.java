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
    public boolean isSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}
