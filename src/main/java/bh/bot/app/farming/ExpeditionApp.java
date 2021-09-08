package bh.bot.app.farming;

import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.ColorizeUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;

@AppMeta(code = "expedition", name = "Expedition", displayOrder = 10)
public class ExpeditionApp extends AbstractDoFarmingApp {
    private byte place = UserConfig.getExpeditionPlaceRange()._1;

    @Override
    protected boolean readMoreInput() throws IOException {
        UserConfig userConfig = getPredefinedUserConfigFromProfileName("You want to do Expedition so you have to specific profile name first!\nSelect an existing profile:");
        try {
            info(ColorizeUtil.formatInfo, "You have selected to farm %s of Expedition", userConfig.getExpeditionPlaceDesc());
            place = userConfig.expeditionPlace;
        } catch (InvalidDataException ex2) {
            warn("You haven't specified an Expedition door to enter so you have to select manually");
            place = selectExpeditionPlace();
        }

        printWarningExpeditionImplementation();
        return true;
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
}
