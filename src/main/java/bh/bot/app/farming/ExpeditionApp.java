package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@AppCode(code = "expedition")
public class ExpeditionApp extends AbstractDoFarmingApp {
    private ExpeditionPlace place = ExpeditionPlace.Astamus;

    @Override
    protected boolean readMoreInput() throws IOException {
        place = selectExpeditionPlace();
        return true;
    }

    @Override
    protected String getAppShortName() {
        return "Expedition";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.expedition;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return ExpeditionApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.enter, false, false),
                new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.Expedition.Buttons.town, true, false),
                new NextAction(BwMatrixMeta.Metas.Invasion.Dialogs.notEnoughBadges, false, true));
    }

    @Override
    protected String getLimitationExplain() {
        return "This function does not support select badge cost, so choose it before turn this on";
    }

    @Override
    protected boolean doCustomAction() {
        return tryEnterExpedition(true, this.place);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum ExpeditionPlace {
        BlubLix, Mowhi, WizBot, Astamus
    }
}
