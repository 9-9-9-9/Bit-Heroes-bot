package bh.bot.app;

import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.tuples.Tuple2;

public class TrialsApp extends AbstractDoFarmingApp {
    @Override
    protected String getAppShortName() {
        return "Trials";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.trials;
    }

    @Override
    protected Tuple2<Boolean, Boolean> isClickedSomething() {
        throw new NotImplementedException();
    }

    @Override
    protected boolean isOutOfTicket() {
        throw new NotImplementedException();
    }

    @Override
    public String getAppCode() {
        return "trials";
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select difficulty and token cost, so choose it before turn this on";
    }
}
