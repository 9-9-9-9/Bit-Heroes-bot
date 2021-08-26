package bh.bot.app;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import static bh.bot.common.Log.debug;

@AppCode(code = "trials")
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
        if (clickImage(BwMatrixMeta.Metas.Trials.Buttons.play)) {
            debug("play");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Trials.Buttons.accept)) {
            debug("accept");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Trials.Buttons.town)) {
            debug("town");
            return new Tuple2<>(true, true);
        }

        return new Tuple2<>(false, false);
    }

    @Override
    protected boolean isOutOfTicket() {
        return clickImage(BwMatrixMeta.Metas.Trials.Dialogs.notEnoughTokens);
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select difficulty and token cost, so choose it before turn this on";
    }
}
