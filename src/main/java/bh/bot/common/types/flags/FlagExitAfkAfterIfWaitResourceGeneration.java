package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagExitAfkAfterIfWaitResourceGeneration extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "ear";
    }

    @Override
    public String getDescription() {
        return "Exit AFK if no more task to do (all out of turns and wait for resource generation)";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}
