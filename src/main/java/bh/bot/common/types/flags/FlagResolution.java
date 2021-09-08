package bh.bot.common.types.flags;

public abstract class FlagResolution extends FlagPattern.NonParamFlag {

    @Override
    public boolean isGlobalFlag() {
        return true;
    }
}
