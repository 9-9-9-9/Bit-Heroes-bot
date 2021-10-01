package bh.bot.common.types.flags;

import bh.bot.common.types.annotations.FlagMeta;

@FlagMeta(cbDesc = "Close game window when bot is going to exit (from v1.7.3 will wait 10 minutes before close)", displayOrder = 5)
public class FlagCloseGameWindowAfterExit extends FlagPattern.NonParamFlag {
    public static final byte waitXMinutes = 10;

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
        return String.format("Close game window when bot is going to exit (wait %d minutes)", waitXMinutes);
    }
}