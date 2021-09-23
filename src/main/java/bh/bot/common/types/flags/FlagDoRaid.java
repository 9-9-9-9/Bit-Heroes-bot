package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta
public class FlagDoRaid extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getDescription() {
        return "Auto doing Raid";
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
