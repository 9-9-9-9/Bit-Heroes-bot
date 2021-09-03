package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagDoTrials extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "trials";
    }

    @Override
    public String getDescription() {
        return "Auto doing Trials";
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