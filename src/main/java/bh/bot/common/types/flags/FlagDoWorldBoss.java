package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

public class FlagDoWorldBoss extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "boss";
    }

    @Override
    public String getDescription() {
        return "Auto doing World Boss";
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
