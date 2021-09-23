package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta
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
    public boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}