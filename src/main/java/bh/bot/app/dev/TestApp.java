package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;

import java.awt.*;

import static bh.bot.common.Log.info;

@AppCode(code = "test")
public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        Tuple2<Point[], Byte> result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
        Point[] points = result._1;
        byte selectedIndex = result._2;

        info("Found %d, selected %d", points.length, selectedIndex + 1);
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
