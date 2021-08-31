package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.InteractionUtil;

import java.awt.*;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppCode(code = "test")
public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        info(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getImageNameCode());
        info(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getWidth() + "x" + BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getHeight());
        info(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getBlackPixelRgb());
        info(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getCoordinateOffset().X);
        info(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.getCoordinateOffset().Y);
        for (int i = 0; i < 3; i++) {
            Point coord = findImage(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog);
            if (coord != null) {
                BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.setLastMatchPoint(coord.x, coord.y);
                long start = System.currentTimeMillis();
                Tuple2<Point[], Byte> result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
                debug("Time: %d ms", System.currentTimeMillis() - start);
                Point[] points = result._1;
                byte selectedIndex = result._2;
                info("Found %d, selected %d", points.length, selectedIndex + 1);
                clickRadioButton(6, points, "Raid");
            } else {
                info("Not found");
            }
        }
    }

    @Override
    protected String getAppName() {
        return "BH-Test code";
    }

    @Override
    protected String getScriptFileName() {
        return "test";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "(developers only) run test code";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
