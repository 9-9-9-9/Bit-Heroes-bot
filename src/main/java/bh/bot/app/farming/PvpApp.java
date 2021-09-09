package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;

import java.util.Arrays;
import java.util.List;

@AppMeta(code = "pvp", name = "PVP", displayOrder = 7)
public class PvpApp extends AbstractDoFarmingApp {
    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.pvp;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return PvpApp.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.PvpArena.Buttons.play, false, false),
                new NextAction(BwMatrixMeta.Metas.PvpArena.Buttons.fight1, false, false),
                new NextAction(BwMatrixMeta.Metas.PvpArena.Buttons.accept, false, false),
                new NextAction(BwMatrixMeta.Metas.PvpArena.Buttons.townOnWin, true, false),
                new NextAction(BwMatrixMeta.Metas.PvpArena.Buttons.townOnLose, true, false),
                new NextAction(BwMatrixMeta.Metas.PvpArena.Dialogs.notEnoughTicket, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function only hit first opponent and does not support select PVP ticket cost, so choose it before turn this on";
    }
}