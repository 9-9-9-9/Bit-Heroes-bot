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

@AppMeta(code = "pvp", name = "PVP", displayOrder = 7)
@RequireSingleInstance
public class PvpApp extends AbstractDoFarmingApp {
    @Override
    protected boolean readMoreInput() throws IOException {
        userConfig = getPredefinedUserConfigFromProfileName("You want to do PVP so you have to specific profile name first!\nSelect an existing profile:");

        try {
            info(ColorizeUtil.formatInfo, "You have chosen to select target on %s of PVP", userConfig.getPvpTargetDesc());
            return true;
        } catch (InvalidDataException ex2) {
            err(ex2.getMessage());
            printRequiresSetting();
            Main.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
            return false;
        }
    }

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
        return "PVP only hit first opponent and does not support select PVP ticket cost, so choose it before turn this on";
    }
}