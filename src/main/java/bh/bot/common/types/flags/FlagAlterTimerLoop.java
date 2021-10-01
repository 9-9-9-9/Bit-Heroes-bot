package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.farming.AbstractDoFarmingApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.types.annotations.FlagMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.StringUtil;
import bh.bot.common.utils.TimeUtil;

import static bh.bot.common.Log.warn;

@FlagMeta(displayOrder = 6)
public class FlagAlterTimerLoop extends FlagPattern<Integer> {
    public static final int defaultValue = -1;
    public static final int minimumValue = 50;
    public static final int maximumValue = 30_000;

    @Override
    protected Integer internalParseParam(String paramPart) throws InvalidFlagException {
        if (StringUtil.isBlank(paramPart))
            return defaultValue;

        Tuple3<Boolean, String, Integer> tuple3 = TimeUtil.tryParseTimeConfig(paramPart, defaultValue);
        if (tuple3._1) {
            final int timer = tuple3._3;
            if (timer < minimumValue)
                throw new InvalidFlagException(String.format("'%s' is an invalid value. Minimum value of flag '%s' is 50ms", paramPart, getCode()));

            if (timer > maximumValue)
                throw new InvalidFlagException(String.format("'%s' is an invalid value. Maximum value of flag '%s' is %ds (%dms)", paramPart, getCode(), maximumValue / 1_000, maximumValue));

            if (timer % 1_000 == 0)
                warn("Main loop timer was modified to %ds by flag '%s'", timer / 1_000, getCode());
            else if (timer > 1_000)
                warn("Main loop timer was modified to %dms (~%d seconds) by flag '%s'", timer, timer /  1_000, getCode());
            else
                warn("Main loop timer was modified to %dms by flag '%s'", timer, getCode());

            return timer;
        }

        throw new InvalidFlagException(String.format("Failed to parse value '%s' of flag '%s' with reason: %s", paramPart, getCode(), tuple3._2));
    }

    @Override
    public boolean isGlobalFlag() {
        return false;
    }

    @Override
    public String getName() {
        return "alter-timer";
    }

    @Override
    public String getDescription() {
        return "(advanced) alter time wait between loop of checking images, if you believe your PC is fast, you can use this flag for a faster progression. Default timer for most functions is 5 seconds, accepted formats are: <number> = number of seconds / <number>s = number of seconds / <number>ms = number of milliseconds, eg: 50ms = loop every 50 milliseconds, or 5000ms equals to 5 seconds";
    }

    @Override
    public boolean isAllowParam() {
        return true;
    }

    @Override
    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AbstractDoFarmingApp
                || instance instanceof AfkApp;
    }
}
