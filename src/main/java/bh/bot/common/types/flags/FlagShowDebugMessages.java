package bh.bot.common.types.flags;

public class FlagShowDebugMessages extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Enable debug messages";
    }

    @Override
    public boolean isDevelopersOnly() {
        return true;
    }
}
