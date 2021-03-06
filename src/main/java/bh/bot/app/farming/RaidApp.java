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
import java.util.function.Supplier;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

@AppMeta(code = "raid", name = "Raid", displayOrder = 6.5)
@RequireSingleInstance
public class RaidApp extends AbstractDoFarmingApp {
    private final Supplier<Boolean> isRaidBlocked = () -> false;
    private UserConfig userConfig;

    @Override
    protected boolean readMoreInput() throws IOException {
        userConfig = getPredefinedUserConfigFromProfileName("You want to do Raid so you have to specific profile name first!\nSelect an existing profile:");

        try {
            info(
                    ColorizeUtil.formatInfo,
                    "You have selected %s mode of %s",
                    userConfig.getRaidModeDesc(),
                    userConfig.getRaidLevelDesc()
            );
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
        return AttendablePlaces.raid;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return RaidApp.getPredefinedImageActions();
    }

    @Override
    protected boolean doCustomAction() {
        return tryEnterRaid(true, userConfig, isRaidBlocked);
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Buttons.town, true, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.rerun, true, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Buttons.accept, false, false),
                new AbstractDoFarmingApp.NextAction(BwMatrixMeta.Metas.Raid.Dialogs.notEnoughShards, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
