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

@AppMeta(code = "bait", name = "Claim Fishing Bait", displayOrder = 2, argRequired = false, argDefault = "2", argType = "number")
@RequireSingleInstance
public class ClaimFishingApp extends AbstractDoFarmingApp {
    private final Supplier<Boolean> isFishingBlocked = () -> false;

    @Override
    protected boolean readMoreInput() throws IOException {
        return true;
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.fishing;
    }

    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return ClaimFishingApp.getPredefinedImageActions();
    }

    @Override
    protected boolean doCustomAction() {
        return tryClaimFishing(true, isFishingBlocked);
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
            // new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.accept, false, false),
            // new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.rerun, true, false),
            // new NextAction(BwMatrixMeta.Metas.Dungeons.Buttons.collect, false, false),
            new NextAction(BwMatrixMeta.Metas.Dungeons.Dialogs.notEnoughEnergy, false, true)
        );
    }

    @Override
    protected String getLimitationExplain() {
        return "Does not fish, just claims bait";
    }

    @Override
    protected int getDefaultMainLoopInterval() {
        return 1_000;
    }
}