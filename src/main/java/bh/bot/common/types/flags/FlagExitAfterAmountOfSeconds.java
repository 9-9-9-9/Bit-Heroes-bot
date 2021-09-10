package bh.bot.common.types.flags;

import bh.bot.app.*;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.InvalidFlagException;

public class FlagExitAfterAmountOfSeconds extends FlagPattern<Integer> {
    private static final byte minimumValue = 60;

    @Override
    protected Integer internalParseParam(String paramPart) throws InvalidFlagException {
        int min = Configuration.enableDevFeatures ? 10 : minimumValue;
        int exitAfter = Integer.parseInt(paramPart);
        if (exitAfter < min)
            throw new InvalidDataException(String.format("Minimum value is %d seconds", min));
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
    public boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof ReRunApp
                || instance instanceof FishingApp
                || instance instanceof AbstractDoFarmingApp
                || instance instanceof AfkApp;
    }
}
