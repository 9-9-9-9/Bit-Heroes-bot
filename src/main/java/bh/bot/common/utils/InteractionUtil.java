package bh.bot.common.utils;

import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.images.ImgMeta;

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
            Log.debug("Send key");
        }

        public static void sendSpaceKey() {
            sendKey(KeyEvent.VK_SPACE);
        }
    }

    public static class Mouse {
        public static void moveCursor(Point p) {
            Log.debug("Mouse move cursor");
            robot.mouseMove(p.x, p.y);
            ThreadUtil.sleep(5);
        }

        public static void mouseClick() {
            int mask = InputEvent.BUTTON1_DOWN_MASK;
            robot.mousePress(mask);
            ThreadUtil.sleep(50);
            robot.mouseRelease(mask);
            Log.debug("Mouse click");
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
    }
}
