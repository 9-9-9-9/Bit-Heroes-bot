package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;

import java.util.Arrays;
import java.util.List;

@AppCode(code = "gauntlet")
public class GauntletApp extends TrialsApp {
    @Override
    protected String getAppShortName() {
        return "Gauntlet";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.gauntlet;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return GauntletApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.Gauntlet.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.Gauntlet.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.Gauntlet.Buttons.town, true, false),
                new NextAction(BwMatrixMeta.Metas.Trials.Dialogs.notEnoughTokens, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select difficulty and token cost, so choose it before turn this on";
    }
}
