package bh.bot.common.types.flags;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;

public class FlagDisableMutex extends FlagPattern.NonParamFlag {
    @Override
    public boolean isGlobalFlag() {
        return true;
    }

    @Override
    public String getName() {
        return "disable-mutex";
    }

    @Override
    public String getDescription() {
        return String.format("Normally, some functions of %s doesn't allow running multiple instances at the same time. You can disable this checking in case it has err and guided to turn off by this bot's creator", Main.botName);
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return true;
    }
}
