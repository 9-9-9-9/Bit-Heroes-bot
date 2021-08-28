package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.GenMiniClient;
import bh.bot.common.exceptions.InvalidFlagException;

public class FlagProfileNo extends FlagPattern<Integer> {
    private final int minimumValue = 1;
    @Override
    protected Integer internalParseParam(String paramPart) throws InvalidFlagException {
        int exitAfter = Integer.parseInt(paramPart);
        if (exitAfter < minimumValue)
            throw new InvalidFlagException(String.format("Minimum value is %d", minimumValue));
        if (exitAfter > GenMiniClient.supportMaximumNumberOfAccounts)
            throw new InvalidFlagException(String.format("Maximum value is %d", GenMiniClient.supportMaximumNumberOfAccounts));
        return exitAfter;
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "Specific profile to load. Used when having multiple accounts configured, default value is 1";
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public <TApp extends AbstractApplication> boolean isSupportedByApp(TApp instance) {
        return instance instanceof AfkApp;
    }
}