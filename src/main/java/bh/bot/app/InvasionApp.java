package bh.bot.app;

import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.tuples.Tuple2;

public class InvasionApp extends AbstractDoFarmingApp {
    @Override
    protected String getAppShortName() {
        return "Invasion";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.invasion;
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
        return "invasion";
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select badge cost, so choose it before turn this on";
    }
}
