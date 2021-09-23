package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(displayOrder = 5)
public class FlagCloseGameWindowAfterExit extends FlagPattern.NonParamFlag {
    @Override
    public boolean isGlobalFlag() {
        return true;
    }

    @Override
    public String getName() {
        return "close-game";
    }

    @Override
    public String getDescription() {
        return "Close game window after bot exit";
    }
}