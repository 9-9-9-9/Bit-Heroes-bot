package bh.bot.app.farming;

import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.debug;

@AppCode(code = "world-boss")
public class WorldBoss extends AbstractDoFarmingApp {
    @Override
    protected String getAppShortName() {
        return "World Boss";
    }

    @Override
    protected AttendablePlace getAttendablePlace() {
        return AttendablePlaces.worldBoss;
    }

    @Override
    protected Tuple2<Boolean, Boolean> isClickedSomething() {
        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss)) {
            debug("summonOnListingPartiesWorldBoss");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses)) {
            debug("summonOnListingWorldBosses");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnSelectingWorldBossTierAndAndDifficulty)) {
            debug("summonOnSelectingWorldBossTierAndAndDifficulty");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.startBoss)) {
            debug("startBoss");
            return new Tuple2<>(true, false);
        }

        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroup)) {
            debug("regroup");
            return new Tuple2<>(true, true);
        }

        if (clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.regroupOnDefeated)) {
            debug("regroupOnDefeated");
            return new Tuple2<>(true, true);
        }

        return new Tuple2<>(false, false);
    }

    @Override
    protected boolean isOutOfTicket() {
        return clickImage(BwMatrixMeta.Metas.WorldBoss.Dialogs.notEnoughXeals);
    }


    @Override
    protected List<NextAction> getInternalPredefinedImageActions() {
        return WorldBoss.getPredefinedImageActions();
    }

    public static List<NextAction> getPredefinedImageActions() {
        return Arrays.asList(
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingPartiesWorldBoss, false, false),
                new NextAction(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses, false, false),
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
