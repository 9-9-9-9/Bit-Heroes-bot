package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.InteractionUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

@AppCode(code = "test")
public class TestApp extends AbstractApplication {
    protected InteractionUtil.Screen.Game gameScreenInteractor = InteractionUtil.Screen.Game.of(this);
    
    @Override
    protected void internalRun(String[] args) {
    	
        Tuple2<Point[], Byte> result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss());
        Point[] points = result._1;
        byte selectedIndex = result._2;
        info("Found %d, selected %d", points.length, selectedIndex + 1);
        clickRadioButton(3, points, "World Boss");

    	final List<AttendablePlace> allAttendablePlaces = Arrays.asList(
                //AttendablePlaces.invasion,
                //AttendablePlaces.trials,
                AttendablePlaces.gvg,
                AttendablePlaces.gauntlet,

                AttendablePlaces.pvp,
                AttendablePlaces.worldBoss,
                AttendablePlaces.raid
        );
    	allAttendablePlaces.forEach(ap -> {
        	Point point = this.gameScreenInteractor.findAttendablePlace(ap);
            if (point != null) {
            	info("%3d, %3d: %s", point.x, point.y, ap.name);
            } else {
            	info("Not found %s", ap.name);
            }
    	});
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
