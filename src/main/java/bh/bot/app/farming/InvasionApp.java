package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;

import java.util.Arrays;
import java.util.List;

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
    protected List<NextAction> getInternalPredefinedImageActions() {
        return InvasionApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.Invasion.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.Invasion.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.Invasion.Buttons.town, true, false),
                new NextAction(BwMatrixMeta.Metas.Invasion.Dialogs.notEnoughBadges, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select badge cost, so choose it before turn this on";
    }
}
