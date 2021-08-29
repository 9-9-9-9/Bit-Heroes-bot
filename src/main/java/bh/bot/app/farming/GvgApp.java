package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.debug;

@AppCode(code = "gvg")
public class GvgApp extends InvasionApp {
    @Override
    protected String getAppShortName() {
        return "GVG";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.gvg;
    }

    @Override
    protected Tuple2<Boolean, Boolean> isClickedSomething() {
        if (clickImage(BwMatrixMeta.Metas.Gvg.Buttons.play)) {
            debug("play");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Gvg.Buttons.fight)) {
            debug("fight");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Gvg.Buttons.accept)) {
            debug("accept");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.Gvg.Buttons.town)) {
            debug("town");
            return new Tuple2<>(true, true);
        }

        return new Tuple2<>(false, false);
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return GvgApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.Gvg.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.Gvg.Buttons.fight, false, false),
                new NextAction(BwMatrixMeta.Metas.Gvg.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.Gvg.Buttons.town, true, false),
                new NextAction(BwMatrixMeta.Metas.Invasion.Dialogs.notEnoughBadges, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select badge cost, so choose it before turn this on";
    }
}