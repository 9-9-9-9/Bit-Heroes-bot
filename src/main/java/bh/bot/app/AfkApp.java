package bh.bot.app;

import bh.bot.common.Configuration;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static bh.bot.common.Log.debug;
import static bh.bot.common.utils.InteractionUtil.Screen.captureScreen;

public class AfkApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
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
                invasion = new Event("invasion-mx.bmp", Ids.Invasion, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Event {
        public final BwMatrixMeta img;
        public final int id;
        public final boolean left;

        public Event(String imgName, int id, boolean left) throws IOException {
            this.img = new BwMatrixMeta(
                    ImageUtil.loadImageFileFromResource(String.format("labels/events/%s", imgName)),
                    new Configuration.Offset(0, 0),
                    0xFFFFFF
            );
            this.id = id;
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
