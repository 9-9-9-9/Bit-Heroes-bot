package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Screen.captureScreen;

public class AfkApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        ArrayList<AttendablePlace> eventList = getAttendablePlaces();
        //
        debug("Scanning screen");
        for (AttendablePlace event : eventList) {
            Point point = findAttendablePlace(event);
            if (point == null)
                continue;
            debug("Found event id %2d at %3d,%3d", event.id, point.x, point.y);
        }
        debug("End");
    }

    private ArrayList<AttendablePlace> getAttendablePlaces() {
        ArrayList<AttendablePlace> eventList = new ArrayList<>();
        //
        if (launchInfo.eInvasion)
            eventList.add(AttendablePlaces.invasion);
        if (launchInfo.eTrials)
            eventList.add(AttendablePlaces.trials);

        if (launchInfo.ePvp)
            eventList.add(AttendablePlaces.pvp);
        if (launchInfo.eWorldBoss)
            eventList.add(AttendablePlaces.worldBoss);
        if (launchInfo.eRaid)
            eventList.add(AttendablePlaces.raid);
        //
        if (eventList.size() == 0) {
            final ArrayList<AttendablePlace> tmpAttendablePlaceList = new ArrayList<>();
            final List<AttendablePlace> allAttendablePlaces = Arrays.asList(
                    AttendablePlaces.invasion,
                    AttendablePlaces.trials,

                    AttendablePlaces.pvp,
                    AttendablePlaces.worldBoss,
                    AttendablePlaces.raid
            );
            info("Select events you want to do:");
            for (AttendablePlace event : allAttendablePlaces.stream().sorted(Comparator.comparingInt(AttendablePlace::getId)).collect(Collectors.toList()))
                info("  %2d. %s", event.id, event.name);
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                while (true) {
                    AttendablePlace event = readInput(br, "Input event code", "To select an event, press the number then press Enter. To finish input, just enter without supply a number", new Function<String, Tuple3<Boolean, String, AttendablePlace>>() {
                        @Override
                        public Tuple3<Boolean, String, AttendablePlace> apply(String s) {
                            try {
                                int result = Integer.parseInt(s);
                                Optional<AttendablePlace> first = allAttendablePlaces.stream().filter(x -> x.id == result).findFirst();
                                if (!first.isPresent())
                                    return new Tuple3<>(false, "ID does not exists", null);
                                AttendablePlace ev = first.get();
                                if (tmpAttendablePlaceList.stream().anyMatch(x -> x.id == ev.id))
                                    return new Tuple3<>(false, String.format("%s had been chosen before", ev.name), null);
                                return new Tuple3<>(true, null, ev);
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse your input, error: " + ex2.getMessage(), null);
                            }
                        }
                    }, true);

                    if (event == null)
                        break;

                    tmpAttendablePlaceList.add(event);
                    info("Selected event %s", event.name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            }

            eventList = new ArrayList<>(tmpAttendablePlaceList.stream().distinct().collect(Collectors.toList()));

            if (eventList.size() == 0) {
                info("No events supplied");
                System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            }
        }

        info("Selected events:");
        for (AttendablePlace event : eventList) {
            info("  <%2d> %s", event.id, event.name);
        }

        return eventList;
    }

    private final int numberOfAttendablePlacesPerColumn = 5;
    private Point findAttendablePlace(AttendablePlace event) {
        int minX, maxX, stepY, firstY;
        if (event.left) {
            Tuple4<Integer, Integer, Integer, Integer> backwardScanLeftAttendablePlaces = Configuration.screenResolutionProfile.getBackwardScanLeftSideAttendablePlaces();
            minX = backwardScanLeftAttendablePlaces._1;
            firstY = backwardScanLeftAttendablePlaces._2;
            stepY = backwardScanLeftAttendablePlaces._3;
            maxX = backwardScanLeftAttendablePlaces._4;
        } else { // right
            Tuple4<Integer, Integer, Integer, Integer> backwardScanRightAttendablePlaces = Configuration.screenResolutionProfile.getBackwardScanRightSideAttendablePlaces();
            minX = backwardScanRightAttendablePlaces._1;
            firstY = backwardScanRightAttendablePlaces._2;
            stepY = backwardScanRightAttendablePlaces._3;
            maxX = backwardScanRightAttendablePlaces._4;
        }
        final int positionTolerant = Math.abs(Math.min(Configuration.Tolerant.position, Math.abs(stepY)));
        final int scanWidth = maxX - minX + 1 + positionTolerant * 2;
        final int scanHeight = Math.abs(stepY) + positionTolerant * 2;
        final int scanX = Math.max(0, minX - positionTolerant);
        for (int i = 0; i < numberOfAttendablePlacesPerColumn; i++) {
            final int scanY = Math.max(0, firstY + stepY * i - positionTolerant);
            BufferedImage sc = captureScreen(scanX, scanY, scanWidth, scanHeight);
            try {
                saveDebugImage(sc, String.format("findAttendablePlace_%d_", i));
                final BwMatrixMeta im = event.img;
                //
                boolean go = true;
                Point p = new Point();
                final int blackPixelRgb = im.getBlackPixelRgb();
                final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
                for (int y = 0; y < sc.getHeight() - im.getHeight() && go; y++) {
                    for (int x = 0; x < sc.getWidth() - im.getWidth() && go; x++) {
                        int rgb = sc.getRGB(x, y) & 0xFFFFFF;
                        if (!im.isMatchBlackRgb(rgb)) {
                            continue;
                        }

                        // debug(String.format("findAttendablePlace first match passed for %d,%d", x, y));
                        boolean allGood = true;

                        for (int[] px : im.getBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (!ImageUtil.areColorsSimilar(//
                                    blackPixelDRgb, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                // debug(String.format("findAttendablePlace second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                break;
                            }
                        }

                        if (allGood) {
                            // debug("findAttendablePlace second match passed");
                            for (int[] px : im.getNonBlackPixels()) {
                                int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                                if (ImageUtil.areColorsSimilar(//
                                        blackPixelRgb, //
                                        srcRgb, //
                                        Configuration.Tolerant.color)) {
                                    allGood = false;
                                    // debug(String.format("findAttendablePlace third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                    break;
                                }
                            }
                        }

                        if (allGood) {
                            // debug("findAttendablePlace third match passed");
                            go = false;
                            p = new Point(scanX + x, scanY + y);
                        }
                    }
                }

                if (!go)
                    return p;
                //

            } finally {
                sc.flush();
            }
        }
        return null;
    }

    @Override
    public String getAppCode() {
        return "afk";
    }

    @Override
    protected String getAppName() {
        return "BH-AFK";
    }

    @Override
    protected String getScriptFileName() {
        return "afk";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Burns your turns while you are AFK";
    }

    @Override
    protected String getFlags() {
        return buildFlags(
                "--invasion : do Invasion",
                "--trials : do Trials",
                "--pvp : do PVP",
                "--boss : do World Boss",
                "--raid : do Raid",
                "--exit=X : exit after X seconds if turns not all consumed"
        );
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
