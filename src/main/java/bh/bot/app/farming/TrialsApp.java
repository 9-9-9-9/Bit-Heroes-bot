package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;

import java.util.Arrays;
import java.util.List;

@AppMeta(code = "trials", name = "Trials", displayOrder = 12)
public class TrialsApp extends AbstractDoFarmingApp {
    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.trials;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return TrialsApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.Trials.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.Trials.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.Trials.Buttons.town, true, false),
                new NextAction(BwMatrixMeta.Metas.Trials.Dialogs.notEnoughTokens, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select difficulty and token cost, so choose it before turn this on";
    }
}
