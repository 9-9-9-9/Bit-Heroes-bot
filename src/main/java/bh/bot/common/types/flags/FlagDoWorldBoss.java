package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta
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
    public boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}
