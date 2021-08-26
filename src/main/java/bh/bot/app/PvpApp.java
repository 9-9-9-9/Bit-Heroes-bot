package bh.bot.app;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import static bh.bot.common.Log.debug;

public class PvpApp extends AbstractDoFarmingApp {
    @Override
    protected String getAppShortName() {
        return "PVP";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.pvp;
    }

    @Override
    protected Tuple2<Boolean, Boolean> isClickedSomething() {
        if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.play)) {
            debug("play");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.fight1)) {
            debug("fight1");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.accept)) {
            debug("accept");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.townOnWin)) {
            debug("townOnWin");
            return new Tuple2<>(true, true);
        }

        if (clickImage(BwMatrixMeta.Metas.PvpArena.Buttons.townOnLose)) {
            debug("townOnLose");
            return new Tuple2<>(true, true);
        }

        return new Tuple2<>(false, false);
    }

    @Override
    protected boolean isOutOfTicket() {
        return clickImage(BwMatrixMeta.Metas.PvpArena.Dialogs.notEnoughTicket);
    }

    @Override
    public String getAppCode() {
        return "pvp";
    }

    @Override
    protected String getLimitationExplain() {
        return "This function only hit first opponent and does not support select PVP ticket cost, so choose it before turn this on";
    }
}
