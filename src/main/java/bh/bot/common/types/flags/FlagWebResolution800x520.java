package bh.bot.common.types.flags;

public class FlagWebResolution800x520 extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "web";
    }

    @Override
    public String getDescription() {
        return "(default) Use mode game resolution 800x520 (Bit Heroes on official website)";
    }
}
