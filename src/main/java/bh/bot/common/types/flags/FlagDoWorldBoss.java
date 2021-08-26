package bh.bot.common.types.flags;

public class FlagDoWorldBoss extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "boss";
    }

    @Override
    public String getDescription() {
        return "Auto doing World Boss";
    }
}
