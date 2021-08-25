package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Screen.captureScreen;

public class AfkApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {

        ArrayList<Event> eventList = new ArrayList<>();
        //
        if (launchInfo.eInvasion)
            eventList.add(Events.invasion);
        //
        if (eventList.size() == 0) {
            final List<Event> allEvents = Arrays.asList(Events.invasion);
            info("Select events you want to do:");
            for (Event event : allEvents)
                info("  %2d. %s", event.id, event.name);
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                while (true) {
                    Event event = readInput(br, "Input event code", "To select an event, press the number then press Enter. To finish input, just enter without supply a number", new Function<String, Tuple3<Boolean, String, Event>>() {
                        @Override
                        public Tuple3<Boolean, String, Event> apply(String s) {
                            try {
                                int result = Integer.parseInt(s);
                                Optional<Event> first = allEvents.stream().filter(x -> x.id == result).findFirst();
                                if (!first.isPresent())
                                    return new Tuple3<>(false, "ID does not exists", null);
                                return new Tuple3<>(true, null, first.get());
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse your input, error: " + ex2.getMessage(), null);
                            }
                        }
                    }, true);

                    if (event == null)
                        break;

                    eventList.add(event);
                    info("Selected event %s", event.name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            }

            eventList = new ArrayList<>(eventList.stream().distinct().collect(Collectors.toList()));

            if (eventList.size() == 0) {
                info("No events supplied");
                System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            }
        }

        info("Selected events:");
        for (Event event : eventList) {
            info("  <%2d> %s", event.id, event.name);
        }
        //
        debug("Scanning screen");
        for (Event event : new Event[]{Events.invasion}) {
            Point point = findEvent(event);
            if (point == null)
                continue;
            debug("Found event id %2d at %3d,%3d", event.id, point.x, point.y);
        }
        debug("End");
    }

    private final int numberOfEventsPerColumn = 5;
    private Point findEvent(Event event) {
        int minX, maxX, stepY, firstY;
        if (event.left) {
            Tuple4<Integer, Integer, Integer, Integer> backwardScanLeftEvents = Configuration.screenResolutionProfile.getBackwardScanLeftEvents();
            minX = backwardScanLeftEvents._1;
            firstY = backwardScanLeftEvents._2;
            stepY = backwardScanLeftEvents._3;
            maxX = backwardScanLeftEvents._4;
        } else { // right
            Tuple4<Integer, Integer, Integer, Integer> backwardScanRightEvents = Configuration.screenResolutionProfile.getBackwardScanRightEvents();
            minX = backwardScanRightEvents._1;
            firstY = backwardScanRightEvents._2;
            stepY = backwardScanRightEvents._3;
            maxX = backwardScanRightEvents._4;
        }
        final int positionTolerant = Math.abs(Math.min(Configuration.Tolerant.position, Math.abs(stepY)));
        final int scanWidth = maxX - minX + 1 + positionTolerant * 2;
        final int scanHeight = Math.abs(stepY) + positionTolerant * 2;
        final int scanX = Math.max(0, minX - positionTolerant);
        for (int i = 0; i < numberOfEventsPerColumn; i++) {
            final int scanY = Math.max(0, firstY + stepY * i - positionTolerant);
            BufferedImage sc = captureScreen(scanX, scanY, scanWidth, scanHeight);
            try {
                saveDebugImage(sc, String.format("findEvent_%d_", i));
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

                        // debug(String.format("findEvent first match passed for %d,%d", x, y));
                        boolean allGood = true;

                        for (int[] px : im.getBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (!ImageUtil.areColorsSimilar(//
                                    blackPixelDRgb, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                // debug(String.format("findEvent second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                break;
                            }
                        }

                        if (allGood) {
                            // debug("findEvent second match passed");
                            for (int[] px : im.getNonBlackPixels()) {
                                int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                                if (ImageUtil.areColorsSimilar(//
                                        blackPixelRgb, //
                                        srcRgb, //
                                        Configuration.Tolerant.color)) {
                                    allGood = false;
                                    // debug(String.format("findEvent third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                    break;
                                }
                            }
                        }

                        if (allGood) {
                            // debug("findEvent third match passed");
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

    public static class Events {
        public static class Ids {
            // Right
            public static final int Invasion = 1;
            public static final int Trials = 2;
            // Left
            public static final int Pvp = 11;
            public static final int WorldBoss = 12;
            public static final int Raid = 13;
        }

        public static Event invasion = null;

        static {
            try {
                invasion = new Event("Invasion", Ids.Invasion, "invasion-mx.bmp", false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Event {
        public final String name;
        public final int id;
        public final BwMatrixMeta img;
        public final boolean left;

        public Event(String name, int id, String imgName, boolean left) throws IOException {
            this.name = name;
            this.id = id;
            this.img = new BwMatrixMeta(
                    ImageUtil.loadImageFileFromResource(String.format("labels/events/%s", imgName)),
                    new Configuration.Offset(0, 0),
                    0xFFFFFF
            );
            this.left = left;
        }
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
