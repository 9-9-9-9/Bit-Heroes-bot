package bh.bot.common.types.flags;

public class FlagDoPvp extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "pvp";
    }

    @Override
    public String getDescription() {
        return "Auto doing PVP";
    }
}
