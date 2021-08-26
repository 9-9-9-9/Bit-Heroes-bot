package bh.bot.common.types.flags;

public class FlagDoInvasion extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "invasion";
    }

    @Override
    public String getDescription() {
        return "Auto doing Invasion";
    }
}
