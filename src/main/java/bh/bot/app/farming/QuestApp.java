package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.ColorizeUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

@AppMeta(code = "quest", name = "Quest", displayOrder = 1)
@RequireSingleInstance
public class QuestApp extends AbstractDoFarmingApp {
    @Override
    protected boolean readMoreInput() throws IOException {
        return true;
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.quest;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return QuestApp.getPredefinedImageActions();
    }

    // @Override
    // protected boolean doCustomAction() {
    //     return true;
    //     // return tryEnterWorldBoss(true, userConfig, isWorldBossBlocked);
    // }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.rerun, false, false),
                new NextAction(BwMatrixMeta.Metas.Dungeons.Dialogs.notEnoughEnergy, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "Quest does not support level select, so choose it before turn this on";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 10_000;
    }
}