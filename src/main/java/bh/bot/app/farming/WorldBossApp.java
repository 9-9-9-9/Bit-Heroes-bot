package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.app.GenMiniClient;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

@AppCode(code = "world-boss")
public class WorldBossApp extends AbstractDoFarmingApp {
    private final Supplier<Boolean> isWorldBossBlocked = () -> false;
    private Configuration.UserConfig userConfig;

    @Override
    protected boolean readMoreInput(BufferedReader br) throws IOException {
        int profileNumber = this.argumentInfo.profileNumber;
        if (profileNumber < 1) {
            info("You want to do WorldBoss so you have to specific profile number first!");
            profileNumber = readInput(br, "Select profile number", String.format("min 1, max %d", GenMiniClient.supportMaximumNumberOfAccounts), new Function<String, Tuple3<Boolean, String, Integer>>() {
                @Override
                public Tuple3<Boolean, String, Integer> apply(String s) {
                    try {
                        int num = Integer.parseInt(s.trim());
                        if (num >= 1 && num <= GenMiniClient.supportMaximumNumberOfAccounts)
                            return new Tuple3<>(true, null, num);
                        return new Tuple3<>(false, "Value must be in range from 1 to " + GenMiniClient.supportMaximumNumberOfAccounts, 0);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, "Not a number", 0);
                    }
                }
            });
        }
        Tuple2<Boolean, Configuration.UserConfig> resultLoadUserConfig = Configuration.loadUserConfig(profileNumber);
        if (!resultLoadUserConfig._1) {
            err("Profile number %d could not be found");
            printRequiresSetting();
            System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
            return false;
        }

        userConfig = resultLoadUserConfig._2;

        try {
            info("You have selected world boss level %s", userConfig.getWorldBossLevelDesc());
            return true;
        } catch (InvalidDataException ex2) {
            err(ex2.getMessage());
            printRequiresSetting();
            System.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
            return false;
        }
    }

    @Override
    protected String getAppShortName() {
        return "World Boss";
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
        return "This function is solo only and does not support select level or type of World Boss, only select by default So which boss do you want to hit? Choose it before turn this on";
    }
}
