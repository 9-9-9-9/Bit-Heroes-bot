package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 10)
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
