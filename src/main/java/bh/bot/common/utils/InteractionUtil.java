package bh.bot.common.utils;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.images.ImgMeta;
import bh.bot.common.types.tuples.Tuple4;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static bh.bot.common.Log.debug;

public class InteractionUtil {
    private static Robot robot;

    public static void init() throws AWTException {
        robot = new Robot();
    }

    public static class Keyboard {
        public static void sendKey(int key) {
            robot.keyPress(key);
            robot.keyRelease(key);
            debug("Send key");
        }

        public static void sendSpaceKey() {
            sendKey(KeyEvent.VK_SPACE);
        }

        public static void sendEscape() {
            sendKey(KeyEvent.VK_ESCAPE);
        }
    }

    public static class Mouse {
        public static void moveCursor(Point p) {
            debug("Mouse move cursor");
            robot.mouseMove(p.x, p.y);
            ThreadUtil.sleep(5);
        }

        public static void mouseClick() {
            int mask = InputEvent.BUTTON1_DOWN_MASK;
            robot.mousePress(mask);
            ThreadUtil.sleep(50);
            robot.mouseRelease(mask);
            debug("Mouse click");
        }

        private static final Point pHideCursor = new Point(950, 100);

        public static void mouseMoveAndClickAndHide(Point p) {
            moveCursor(p);
            ThreadUtil.sleep(100);
            mouseClick();
            ThreadUtil.sleep(200);
            moveCursor(p);
            ThreadUtil.sleep(100);
            mouseClick();
            ThreadUtil.sleep(200);
            moveCursor(pHideCursor);
        }
    }

    public static class Screen {
        public static ScreenCapturedResult captureElementInEstimatedArea(ImgMeta im) {
            return captureElementInEstimatedArea(im.getCoordinateOffset(), im.getWidth(), im.getHeight());
        }

        public static ScreenCapturedResult captureElementInEstimatedArea(BwMatrixMeta im) {
            return captureElementInEstimatedArea(im.getCoordinateOffset(), im.getWidth(), im.getHeight());
        }

        private static ScreenCapturedResult captureElementInEstimatedArea(Configuration.Offset coordinate, int w, int h) {
            int xS = Math.max(0, Configuration.Offsets.gameScreenOffset.X + coordinate.X - Configuration.Tolerant.position);
            int yS = Math.max(0, Configuration.Offsets.gameScreenOffset.Y + coordinate.Y - Configuration.Tolerant.position);
            int wS = w + Configuration.Tolerant.position * 2;
            int hS = h + Configuration.Tolerant.position * 2;
            return new ScreenCapturedResult(captureScreen(xS, yS, wS, hS), xS, yS);
        }

        public static BufferedImage captureScreen(int x, int y, int w, int h) {
            return robot.createScreenCapture(new Rectangle(x, y, w, h));
        }

        public static Color getPixelColor(Point p) {
            return robot.getPixelColor(p.x, p.y);
        }

        public static Color getPixelColor(int x, int y) {
            return robot.getPixelColor(x, y);
        }

        public static class ScreenCapturedResult {
            public BufferedImage image;
            public int x;
            public int y;
            public int w;
            public int h;

            public ScreenCapturedResult(BufferedImage image, int x, int y) {
                this.image = image;
                this.x = x;
                this.y = y;
                this.w = image.getWidth();
                this.h = image.getHeight();
            }
        }

        public static class Game<T extends AbstractApplication> {
            private final T instance;

            private final int numberOfAttendablePlacesPerColumn = 5;

            private Game(T instance) {
                this.instance = instance;
            }

            public static <T extends AbstractApplication> Game of(T instance) {
                return new Game(instance);
            }

            public <T extends AbstractApplication> Point findAttendablePlace(AttendablePlace event) {
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
                        instance.saveDebugImage(sc, String.format("findAttendablePlace_%d_", i));
                        final BwMatrixMeta im = event.img;
                        //
                        boolean go = true;
                        Point p = new Point();
                        final int blackPixelRgb = im.getBlackPixelRgb();
                        final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
                        for (int y = 0; y < sc.getHeight() - im.getHeight() && go; y++) {
                            for (int x = 0; x < sc.getWidth() - im.getWidth() && go; x++) {
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
        }
    }
}
