package bh.bot.common.types.flags;

public class FlagDoTrials extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "trials";
    }

    @Override
    public String getDescription() {
        return "Auto doing Trials";
    }
}