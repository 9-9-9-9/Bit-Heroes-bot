package bh.bot.common.types.flags;

public class FlagMuteNoti extends FlagPattern.NonParamFlag {
    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Do not publish notification to Telegram channel";
    }

    @Override
    public boolean isGlobalFlag() {
        return true;
    }
}
