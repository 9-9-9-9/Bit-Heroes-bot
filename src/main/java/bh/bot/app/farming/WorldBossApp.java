package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static bh.bot.Main.colorFormatInfo;
import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

@AppMeta(code = "world-boss", name = "World Boss", displayOrder = 6)
public class WorldBossApp extends AbstractDoFarmingApp {
    private final Supplier<Boolean> isWorldBossBlocked = () -> false;
    private UserConfig userConfig;

    @Override
    protected boolean readMoreInput() throws IOException {
        int profileNumber = this.argumentInfo.profileNumber;
        if (profileNumber < 1)
            profileNumber = readProfileNumber("You want to do World Boss so you have to specific profile number first!\nSelect profile number");
        Tuple2<Boolean, UserConfig> resultLoadUserConfig = Configuration.loadUserConfig(profileNumber);
        if (!resultLoadUserConfig._1) {
            err("Profile number %d could not be found");
            printRequiresSetting();
            System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
            return false;
        }

        userConfig = resultLoadUserConfig._2;

        try {
            info(colorFormatInfo, "You have selected world boss level %s", userConfig.getWorldBossLevelDesc());
            return true;
        } catch (InvalidDataException ex2) {
            err(ex2.getMessage());
            printRequiresSetting();
            System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
            return false;
        }
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.worldBoss;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return WorldBossApp.getPredefinedImageActions();
    }

    @Override
    protected boolean doCustomAction() {
        return tryEnterWorldBoss(true, userConfig, isWorldBossBlocked);
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss, false, false),
                // new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses, false, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty, false, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss, false, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.regroup, true, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.regroupOnDefeated, true, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Dialogs.notEnoughXeals, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "This function is solo only and does not support select mode of World Boss (Normal/Hard/Heroic), only select by default So which boss do you want to hit? Choose it before turn this on";
    }
}
