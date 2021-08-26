package bh.bot.common.types.flags;

public class FlagSteamResolution800x480 extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "steam";
    }

    @Override
    public String getDescription() {
        return "Use mode game resolution 800x480 (Bit Heroes on Steam)";
    }
}
