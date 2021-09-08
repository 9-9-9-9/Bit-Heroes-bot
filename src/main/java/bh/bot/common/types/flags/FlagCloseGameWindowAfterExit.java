package bh.bot.common.types.flags;

public class FlagCloseGameWindowAfterExit extends FlagPattern.NonParamFlag {
    @Override
    public boolean isGlobalFlag() {
        return true;
    }

    @Override
    public String getName() {
        //noinspection SpellCheckingInspection
        return "close-game";
    }

    @Override
    public String getDescription() {
        return "Close game window after bot exit";
    }
}