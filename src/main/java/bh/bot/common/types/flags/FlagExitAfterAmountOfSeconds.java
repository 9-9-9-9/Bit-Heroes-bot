package bh.bot.common.types.flags;

import bh.bot.app.*;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.common.exceptions.InvalidFlagException;

public class FlagExitAfterAmountOfSeconds extends FlagPattern<Integer> {
    private static final byte minimumValue = 60;

    @Override
    protected Integer internalParseParam(String paramPart) throws InvalidFlagException {
        int exitAfter = Integer.parseInt(paramPart);
        if (exitAfter < minimumValue)
            throw new InvalidFlagException(String.format("Minimum value is %d seconds", minimumValue));
        return exitAfter;
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public boolean isAllowParam() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Exit after amount of seconds, no matter if task hasn't been completed";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public <TApp extends AbstractApplication> boolean internalCheckIsSupportedByApp(TApp instance) {
        return instance instanceof ReRunApp
                || instance instanceof FishingApp
                || instance instanceof AbstractDoFarmingApp
                || instance instanceof AfkApp;
    }
}
