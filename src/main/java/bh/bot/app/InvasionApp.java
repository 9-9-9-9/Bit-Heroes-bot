package bh.bot.app;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import static bh.bot.common.Log.debug;

@AppCode(code = "invasion")
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
        if (clickImage(BwMatrixMeta.Metas.Invasion.Buttons.play)) {
            debug("play");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Invasion.Buttons.accept)) {
            debug("accept");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Invasion.Buttons.town)) {
            debug("town");
            return new Tuple2<>(true, true);
        }

        return new Tuple2<>(false, false);
    }

    @Override
    protected boolean isOutOfTicket() {
        return clickImage(BwMatrixMeta.Metas.Invasion.Dialogs.notEnoughBadges);
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select badge cost, so choose it before turn this on";
    }
}
