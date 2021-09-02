package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.InteractionUtil.Keyboard.sendSpaceKey;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.InteractionUtil.Screen.captureScreen;
import static bh.bot.common.utils.InteractionUtil.Screen.getPixelColor;
import static bh.bot.common.utils.ThreadUtil.sleep;
import static bh.bot.common.utils.ThreadUtil.waitDone;

@AppCode(code = "fishing")
public class FishingApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        int arg;

        try (
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
        ) {
            try {
                arg = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                info(getHelp());
                arg = readInput(br, "How many times do you want to hook?", "Numeric only", s -> {
                    try {
                        int num = Integer.parseInt(s);
                        if (num < 1) {
                            return new Tuple3<>(false, "Must greater than 0", 0);
                        }
                        return new Tuple3<>(true, null, num);
                    } catch (NumberFormatException ex1) {
                        return new Tuple3<>(false, "The value you inputted is not a number", 0);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
            throw new RuntimeException(e);
        }

        final int loop = arg;
        info("Loop: %d", loop);

        int retry = 5;
        Point labelFishingCord;
        while ((labelFishingCord = detectLabelFishing()) == null && retry-- > 0) {
            err("Unable to detect the FISHING label of fishing scene");
            info("Retrying to detect the FISHING label of fishing scene");
            sleep(5000);
        }

        if (labelFishingCord == null) {
            info("Exiting");
            System.exit(Main.EXIT_CODE_UNABLE_DETECTING_FISHING_ANCHOR);
        }

        debug("labelFishingCord: %3d, %3d", labelFishingCord.x, labelFishingCord.y);

        Configuration.Offset offsetLabelFishing = Configuration.screenResolutionProfile.getOffsetLabelFishing();
        final Point anchorPoint = new Point(
                labelFishingCord.x - offsetLabelFishing.X,
                labelFishingCord.y - offsetLabelFishing.Y
        );

        info("Anchor point: %d,%d", anchorPoint.x, anchorPoint.y);

        AtomicBoolean masterSwitch = new AtomicBoolean(false);
        AtomicInteger screen = new AtomicInteger(screenNone);
        AtomicBoolean unsure = new AtomicBoolean(true);
        AtomicLong unsureFrom = new AtomicLong(Long.MAX_VALUE);
        AtomicLong seeBtnStartFrom = new AtomicLong(Long.MAX_VALUE);
        waitDone(
                () -> doLoopFishing(loop, masterSwitch, anchorPoint, screen, unsure, unsureFrom),
                () -> detectLongTimeNoSee(masterSwitch, unsureFrom, seeBtnStartFrom),
                () -> detectScreen(masterSwitch, anchorPoint, screen, unsure, unsureFrom, seeBtnStartFrom),
                () -> detectDisconnected(masterSwitch),
                () -> autoExit(argumentInfo.exitAfterXSecs, masterSwitch),
                () -> doCheckGameScreenOffset(masterSwitch)
        );
        Telegram.sendMessage("Stopped", false);
    }

    private void detectLongTimeNoSee(final AtomicBoolean masterSwitch, final AtomicLong unsureFrom, final AtomicLong seeBtnStartFrom) {
        try {
            final long MAX_TIME = 300_000;
            while (!masterSwitch.get()) {
                long now = System.currentTimeMillis();
                long usf = now - unsureFrom.get();
                long ssf = now - seeBtnStartFrom.get();

                if (usf < MAX_TIME && ssf < MAX_TIME) {
                    sleep(60_000);
                    continue;
                }

                String msg;
                if (usf >= MAX_TIME) {
                    msg = "Unable to continue (unsure)";
                } else {
                    msg = "Unable to continue (stuck at Start button)";
                }

                info(msg);
                Telegram.sendMessage(msg, true);

                masterSwitch.set(true);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    private final int screenNone = 0;
    private final int screenStart = 1;
    private final int screenCast = 2;
    private final int screenCatch = 3;

    private void doLoopFishing(int loopCount, final AtomicBoolean masterSwitch, final Point anchorPoint, final AtomicInteger screen, final AtomicBoolean unsure, final AtomicLong unsureFrom) {
        info("Start Fishing");
        try {
            moveCursor(new Point(950, 100));

            final int xButton1 = anchorPoint.x + BwMatrixMeta.Metas.Fishing.Buttons.start.getCoordinateOffset().X - 40;
            final int yButton1 = anchorPoint.y + BwMatrixMeta.Metas.Fishing.Buttons.start.getCoordinateOffset().Y;
            final Point pButton1 = new Point(xButton1, yButton1);

            boolean requestedToExit = false;

            while (!masterSwitch.get()) {
                sleep(50);

                if (loopCount < 1 && !requestedToExit)
                    requestedToExit = true;

                final int curScreen = screen.get();
                if (curScreen == screenNone) {
                    sleep(2000);
                    continue;
                }

                if (unsure.get()) {
                    sendSpaceKey();
                    sleep(4000);
                    continue;
                }

                if (curScreen == screenCatch) {
                    debug("On screen CATCH");

                    Configuration.Offset offsetDetect100PcCatchingFish = Configuration.screenResolutionProfile.getOffsetDetect100PcCatchingFish();
                    Color color = getPixelColor(
                            anchorPoint.x + offsetDetect100PcCatchingFish.X,
                            anchorPoint.y + offsetDetect100PcCatchingFish.Y
                    );
                    if (color.getGreen() < 230)
                        continue;
                    if (color.getRed() > 100)
                        continue;
                    if (color.getBlue() > 100)
                        continue;

                    debug("Catch %3d,%3d,%3d", color.getRed(), color.getGreen(), color.getBlue());

                    sendSpaceKey();
                    unsure.set(true);
                    unsureFrom.set(System.currentTimeMillis());
                    moveCursor(pButton1);
                    if (requestedToExit)
                        break;
                    mouseClick();
                    sendSpaceKey();
                    sleep(1500);

                } else if (curScreen == screenCast) {
                    debug("On screen CAST");

                    Configuration.Offset offsetScanCastingFish = Configuration.screenResolutionProfile.getOffsetScanCastingFish();
                    Configuration.Size scanSizeCastingFish = Configuration.screenResolutionProfile.getScanSizeCastingFish();
                    BufferedImage sc = captureScreen(
                            anchorPoint.x + offsetScanCastingFish.X,
                            anchorPoint.y + offsetScanCastingFish.Y,
                            scanSizeCastingFish.W,
                            scanSizeCastingFish.H
                    );
                    final int black = 0x000000;
                    try {
                        final int offset1 = 0;
                        final int offset2 = scanSizeCastingFish.H - 1;
                        final int offsetSize = scanSizeCastingFish.H / 4 - 2;
                        final int offset3 = offset1 + offsetSize;
                        final int offset4 = offset2 - offsetSize;
                        for (int x = 0; x < sc.getWidth(); x++) {
                            if ((sc.getRGB(x, offset3) & 0xFFFFFF) != black) {
                                debug("Fail check CAST step p1: %s at %3d,%3d", Integer.toHexString((sc.getRGB(x, offset3) & 0xFFFFFF)), x, offset3);
                                continue;
                            }
                            if ((sc.getRGB(x, offset4) & 0xFFFFFF) != black) {
                                debug("Fail check CAST step p2: %s at %3d,%3d", Integer.toHexString((sc.getRGB(x, offset4) & 0xFFFFFF)), x, offset4);
                                continue;
                            }
                            if ((sc.getRGB(x, offset1) & 0xFFFFFF) != black) {
                                debug("Fail check CAST step p3: %s at %3d,%3d", Integer.toHexString((sc.getRGB(x, offset1) & 0xFFFFFF)), x, offset1);
                                continue;
                            }
                            if ((sc.getRGB(x, offset2) & 0xFFFFFF) != black) {
                                debug("Fail check CAST step p4: %s at %3d,%3d", Integer.toHexString((sc.getRGB(x, offset2) & 0xFFFFFF)), x, offset2);
                                continue;
                            }

                            debug("Good, casting now");

                            sendSpaceKey();
                            unsure.set(true);
                            unsureFrom.set(System.currentTimeMillis());

                            loopCount--;
                            info("Remaining: %d", loopCount);

                            sleep(1500);
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        sleep(500);
                    } finally {
                        freeMem(sc);
                    }

                } else if (curScreen == screenStart) {
                    debug("On screen START");
                    moveCursor(pButton1);
                    mouseClick();
                    sleep(1500);
                }
            }

            masterSwitch.set(true);
            info("Fishing has completed");
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    private void detectScreen(final AtomicBoolean masterSwitch, final Point anchorPoint, final AtomicInteger screen, final AtomicBoolean unsure, final AtomicLong unsureFrom, final AtomicLong seeBtnStartFrom) {
        try {
            while (!masterSwitch.get()) {
                sleep(1000);

                BufferedImage sc = captureScreen(
                        anchorPoint.x,
                        anchorPoint.y,
                        Configuration.screenResolutionProfile.getSupportedGameResolutionWidth(),
                        Configuration.screenResolutionProfile.getSupportedGameResolutionHeight()
                );
                try {
                    saveDebugImage(sc, "detectScreen_fishing");

                    if (isContains(sc, BwMatrixMeta.Metas.Fishing.Buttons.catch_)) {
                        screen.set(screenCatch);
                        unsure.set(false);
                        unsureFrom.set(Long.MAX_VALUE);
                        seeBtnStartFrom.set(Long.MAX_VALUE);
                        continue;
                    }

                    if (isContains(sc, BwMatrixMeta.Metas.Fishing.Buttons.cast)) {
                        screen.set(screenCast);
                        unsure.set(false);
                        unsureFrom.set(Long.MAX_VALUE);
                        seeBtnStartFrom.set(Long.MAX_VALUE);
                        continue;
                    }

                    if (isContains(sc, BwMatrixMeta.Metas.Fishing.Buttons.start)) {
                        screen.set(screenStart);
                        unsure.set(false);
                        unsureFrom.set(Long.MAX_VALUE);
                        seeBtnStartFrom.set(System.currentTimeMillis());
                        continue;
                    }

                    unsure.set(true);
                    unsureFrom.set(System.currentTimeMillis());
                    seeBtnStartFrom.set(Long.MAX_VALUE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    sleep(2000);
                } finally {
                    freeMem(sc);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    private boolean isContains(BufferedImage sc, BwMatrixMeta im) {
        return isContains(sc, im, false);
    }

    private boolean isContains(BufferedImage sc, BwMatrixMeta im, boolean debug) {
        if (im.throwIfNotAvailable())
        	return false;

        final int offsetX = im.getCoordinateOffset().X;
        final int offsetY = im.getCoordinateOffset().Y;
        final int colorTolerant = Configuration.Tolerant.color;
        final int blackPixelRgb = im.getBlackPixelRgb();
        final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();

        for (int[] px : im.getBlackPixels()) {
            if (!ImageUtil.areColorsSimilar(//
                    blackPixelDRgb, //
                    sc.getRGB(offsetX + px[0], offsetY + px[1]) & 0xFFFFFF, //
                    colorTolerant)) {
                if (debug)
                    debug("Fail (1) at %3d, %3d (offset=%3d, %3d, coor=%3d, %3d) with color: %d vs %d", offsetX + px[0], offsetY + px[1], offsetX, offsetY, px[0], px[1], blackPixelRgb, sc.getRGB(offsetX + px[0], offsetY + px[1]) & 0xFFFFFF);
                return false;
            }
        }

        for (int[] px : im.getNonBlackPixels()) {
            if (ImageUtil.areColorsSimilar(//
                    blackPixelRgb, //
                    sc.getRGB(offsetX + px[0], offsetY + px[1]) & 0xFFFFFF, //
                    colorTolerant)) {
                if (debug)
                    debug("Fail (2) at %3d, %3d (offset=%3d, %3d, coor=%3d, %3d) with color: %d vs %d", offsetX + px[0], offsetY + px[1], offsetX, offsetY, px[0], px[1], blackPixelRgb, sc.getRGB(offsetX + px[0], offsetY + px[1]) & 0xFFFFFF);
                return false;
            }
        }

        return true;
    }

    @Override
    protected String getAppName() {
        return "BH-Fishing";
    }

    @Override
    protected String getScriptFileName() {
        return "fishing";
    }

    @Override
    protected String getUsage() {
        return "<count>";
    }

    @Override
    protected String getDescription() {
        return "Do fishing";
    }

    @Override
    protected String getLimitationExplain() {
        return "To start using this function, you the to be ready in fishing state, and the Start button is clearly visible on the screen";
    }
}
