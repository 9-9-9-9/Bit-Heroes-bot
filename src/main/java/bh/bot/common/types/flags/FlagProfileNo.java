package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.GenMiniClient;
import bh.bot.app.farming.RaidApp;
import bh.bot.app.farming.WorldBossApp;
import bh.bot.common.exceptions.InvalidFlagException;

public class FlagProfileNo extends FlagPattern<Integer> {
    @Override
    protected Integer internalParseParam(String paramPart) throws InvalidFlagException {
        int profileNo = Integer.parseInt(paramPart);
        int minimumValue = 1;
        if (profileNo < minimumValue)
            throw new InvalidFlagException(String.format("Minimum value of flag '--%s' is %d", getName(), minimumValue));
        if (profileNo > GenMiniClient.supportMaximumNumberOfAccounts)
            throw new InvalidFlagException(String.format("Maximum value of flag '--%s' is %d", getName(), GenMiniClient.supportMaximumNumberOfAccounts));
        return profileNo;
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
    public boolean isSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp || instance instanceof WorldBossApp || instance instanceof RaidApp;
    }

    @Override
    public boolean isAllowParam() {
        return true;
    }
}