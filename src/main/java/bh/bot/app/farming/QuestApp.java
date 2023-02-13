package bh.bot.app.farming;

import bh.bot.Main;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.QuestOrder;
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

@AppMeta(code = "quest", name = "Quest", displayOrder = 1, argRequired = false, argDefault = "2", argType = "number")
@RequireSingleInstance
public class QuestApp extends AbstractDoFarmingApp {
    private final Supplier<Boolean> isQuestBlocked = () -> false;
    private UserConfig userConfig;
    private String questStrings = "";

    @Override
    protected boolean readMoreInput() throws IOException {
        userConfig = getPredefinedUserConfigFromProfileName("You want to do Quests so you have to specific profile name first!\nSelect an existing profile:");

        try {
            info(
                    ColorizeUtil.formatInfo,
                    "You have selected %s mode",
                    userConfig.getQuestModeDesc()
            );
            info(
                    ColorizeUtil.formatInfo,
                    "You have selected %s order",
                    userConfig.getQuestOrderDesc()
            );
            for (int i = 0; i < userConfig.questOrder.length(); i++) {
                char charKey = userConfig.questOrder.charAt(i);
                if (charKey == QuestOrder.Dungeons || charKey == QuestOrder.FilledStars || charKey == QuestOrder.EmptyStars ||charKey == QuestOrder.Flags) {
                    questStrings += charKey;
                }
            }
            if (questStrings == "") {
                questStrings = QuestOrder.defaultOrder;
            }
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
        return AttendablePlaces.quest;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return QuestApp.getPredefinedImageActions();
    }

    @Override
    protected boolean doCustomAction() {
        return tryEnterQuest(true, userConfig, isQuestBlocked, this.gameScreenInteractor, this.questStrings);
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
            new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.accept, false, false),
            new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.rerun, true, false),
            new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.collect, false, false),
            new NextAction(BwMatrixMeta.Metas.Dungeons.Dialogs.notEnoughEnergy, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "Quest does not support level select, so choose it before turn this on";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 1_000;
    }
}