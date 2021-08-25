package bh.bot.app;

import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.images.ImgMeta;
import bh.bot.common.types.images.Pixel;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.ThreadUtil;
import bh.bot.common.types.tuples.Tuple3;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseMoveAndClickAndHide;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.InteractionUtil.Screen.*;
import static bh.bot.common.utils.StringUtil.isBlank;

public abstract class AbstractApplication {
    public static LaunchInfo parse(String[] args) {
        String appCode = args[0];

        List<String> listArg = Arrays
                .stream(args)
                .map(x -> x.toLowerCase().trim())
                .collect(Collectors.toList());

        boolean enableSavingDebugImages = false;
        if (listArg.contains("--img"))
            enableSavingDebugImages = true;
        if (listArg.contains("--debug"))
            Log.enableDebug();
        if (listArg.contains("--mute"))
            Telegram.disable();
        else if (Telegram.isDisabled())
            Log.info("Telegram is disabled due to missing or invalid configuration");

        if (listArg.contains("--exit"))
            Log.err("Invalid usage of flag 'exit', should be '--exit=X' where X is the number of seconds to await before force exit, for example: '--exit=3600' means exit after 1 hours");
        List<String> exitCmd = listArg.stream().filter(x -> x.startsWith("--exit=") && x.length() > 7).collect(Collectors.toList());
        int exitAfter = 0;
        if (exitCmd.size() > 0) {
            String sExitAfter = exitCmd.get(0).substring(7);
            try {
                exitAfter = Math.max(0, Integer.parseInt(sExitAfter));
                if (exitAfter > 3600) {
                    int h = exitAfter / 3600;
                    int m = (exitAfter - h * 3600) / 60;
                    Log.info("Application will exit after %d hours and %d minutes", h, m);
                }
            } catch (NumberFormatException e) {
                Log.err("Can not parse command: --exit=%s", sExitAfter);
            }
        }

        boolean displayHelp = listArg.contains("--help");

        args = Arrays
                .stream(args)
                .skip(1)
                .filter(x -> !x.startsWith("--"))
                .toArray(String[]::new);
        AbstractApplication instance = Configuration.getInstanceFromAppCode(appCode);

        if (instance == null)
            throw new IllegalArgumentException("First argument must be a valid app name");

        LaunchInfo li = new LaunchInfo(instance, args);
        li.exitAfterXSecs = exitAfter;
        li.displayHelp = displayHelp;
        li.enableSavingDebugImages = enableSavingDebugImages;
        return li;
    }

    public static class LaunchInfo {
        public AbstractApplication instance;
        public String[] arguments;
        public int exitAfterXSecs;
        public boolean displayHelp;
        public boolean enableSavingDebugImages;

        public LaunchInfo(AbstractApplication instance, String[] args) {
            this.instance = instance;
            this.arguments = args;
        }
    }

    protected int exitAfterXSecs = 0;
    protected boolean enableSavingDebugImages = false;

    public void run(LaunchInfo launchInfo) throws Exception {
        this.exitAfterXSecs = launchInfo.exitAfterXSecs;
        this.enableSavingDebugImages = launchInfo.enableSavingDebugImages;
        if (this.enableSavingDebugImages)
            Log.info("Enabled saving debug images");
        initOutputDirectories();
        ImgMeta.load();
        BwMatrixMeta.load();
        Telegram.setAppName(getAppName());
        internalRun(launchInfo.arguments);
    }

    private void initOutputDirectories() {
        mkdir("out");
        mkdir("out", "images");
        mkdir("out", "images", getAppCode());
    }

    private void mkdir(String path, String... paths) {
        File file = Paths.get(path, paths).toFile();
        if (!file.exists())
            file.mkdir();
    }

    protected BufferedImage loadImageFromFile(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    protected void saveDebugImage(BufferedImage img, String prefix) {
        if (!enableSavingDebugImages) {
            return;
        }
        File file = Paths.get("out", "images", getAppCode(), "dbg_" + prefix + "_" + System.currentTimeMillis() + ".bmp").toFile();
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.err("Unable to save image file '%s' to %s", file.getName(), file.toString());
        }
    }

    protected void saveImage(BufferedImage img, String prefix) {
        File file = Paths.get("out", "images", getAppCode(), prefix + "_" + System.currentTimeMillis() + ".bmp").toFile();
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.err("Unable to save image file '%s' to %s", file.getName(), file.toString());
        }
    }

    protected abstract void internalRun(String[] args);

    public abstract String getAppCode();

    protected abstract String getAppName();

    protected abstract String getScriptFileName();

    protected String getScriptName() {
        if (Configuration.OS.isWin)
            return ".\\" + getScriptFileName() + ".bat";
        return "./" + getScriptFileName() + ".sh";
    }

    protected abstract String getUsage();

    protected abstract String getDescription();

    protected String buildFlags(String... flags) {
        if (flags.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (String flag : flags) {
            sb.append("\n  ");
            sb.append(flag);
        }
        return sb.toString();
    }

    protected abstract String getFlags();

    public String getHelp() {
        StringBuilder sb = new StringBuilder(getAppName());
        sb.append("\n  Description: ");
        sb.append(getDescription());
        sb.append("\nUsage:\n");
        sb.append(getScriptName());
        String usage = getUsage();
        if (usage != null) {
            sb.append(' ');
            sb.append(usage);
        }
        sb.append(" [additional flags]");
        String flags = getFlags();
        if (flags != null) {
            sb.append("\nFlags:");
            sb.append(flags);
        }
        sb.append("\nGlobal flags:");
        sb.append("\n  --mute : do not send notification messages to Telegram channel");
        sb.append("\n  --debug : print debug messages (developers only)");
        sb.append("\n  --img : save screen captured pictures to disk (developers only)");
        return sb.toString();
    }

    protected void doLoopClickImage(int loopCount, AtomicBoolean masterSwitch) {
        moveCursor(new Point(950, 100));
        long lastFound = System.currentTimeMillis();
        while (loopCount > 0 && !masterSwitch.get()) {
            if (clickImage()) {
                loopCount--;
                lastFound = System.currentTimeMillis();
                Log.info("%d loop left", loopCount);
                ThreadUtil.sleep(10000);
            } else {
                Log.debug("Not found, repeat");
                ThreadUtil.sleep(10000);
                if (System.currentTimeMillis() - lastFound > 900000) {
                    Log.info("Long time no see => Stop");
                    Telegram.sendMessage("long time no see button", true);
                    break;
                }
            }
        }

        masterSwitch.set(true);
    }

    protected boolean clickImage() {
        throw new NotImplementedException();
    }

    protected boolean clickImage(ImgMeta im) {
        return clickImageExact(im) || clickImageScanBW(im);
    }

    protected boolean clickImageExact(ImgMeta im) {
        int[] lastMatch = im.getLastMatchPoint();
        if (lastMatch[0] < 0 || lastMatch[1] < 0) {
            return false;
        }

        Point p = new Point(lastMatch[0], lastMatch[1]);
        Color c = getPixelColor(p);
        if (!im.isMatchFirstRgb(c.getRGB())) {
            return false;
        }

        BufferedImage sc = captureScreen(lastMatch[0], lastMatch[1], im.getWidth(), im.getHeight());

        try {
            for (Pixel px : im.getPixelList()) {
                int srcRgb = sc.getRGB(px.x, px.y) & 0xFFFFFF;
                if (!ImageUtil.areColorsSimilar(//
                        px.rgb, //
                        srcRgb, //
                        Configuration.Tolerant.color)) {
                    return false;
                }
            }

            Log.debug("Success on last click");
            mouseMoveAndClickAndHide(p);
            return true;
        } finally {
            sc.flush();
        }
    }

    protected boolean clickImageScanBW(ImgMeta im) {
        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im);
        BufferedImage sc = screenCapturedResult.image;

        try {
            saveDebugImage(sc, "clickImageScan");

            boolean go = true;
            Point p = new Point();
            for (int y = 0; y < sc.getHeight() - im.getHeight() && go; y++) {
                for (int x = 0; x < sc.getWidth() - im.getWidth() && go; x++) {
                    int rgb = sc.getRGB(x, y);
                    if (!im.isMatchFirstRgb(rgb)) {
                        continue;
                    }

                    boolean allGood = true;

                    for (Pixel px : im.getPixelList()) {
                        int srcRgb = sc.getRGB(x + px.x, y + px.y) & 0xFFFFFF;
                        if (!ImageUtil.areColorsSimilar(//
                                px.rgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            break;
                        }
                    }

                    if (allGood) {
                        go = false;
                        p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                    }
                }
            }

            if (!go) {
                mouseMoveAndClickAndHide(p);
                im.setLastMatchPoint(p.x, p.y);
                return true;
            }

            return false;
        } finally {
            sc.flush();
        }
    }

    protected boolean clickImage(BwMatrixMeta im) {
        return clickImageExact(im) || clickImageScanBW(im);
    }

    protected boolean clickImageExact(BwMatrixMeta im) {
        int[] lastMatch = im.getLastMatchPoint();
        if (lastMatch[0] < 0 || lastMatch[1] < 0) {
            return false;
        }

        Point p = new Point(lastMatch[0], lastMatch[1]);
        Color c = getPixelColor(p);
        if (!im.isMatchBlackRgb(c.getRGB())) {
            return false;
        }
        // debug("clickImageExactBW match success 1");

        BufferedImage sc = captureScreen(lastMatch[0], lastMatch[1], im.getWidth(), im.getHeight());

        try {
            int blackPixelRgb = im.getBlackPixelRgb();
            ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int[] px : im.getBlackPixels()) {
                if (!ImageUtil.areColorsSimilar(//
                        blackPixelDRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return false;
                }
            }

            // debug("clickImageExactBW match success 2");

            for (int[] px : im.getNonBlackPixels()) {
                if (ImageUtil.areColorsSimilar(//
                        blackPixelRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return false;
                }
            }

            // debug("clickImageExactBW match success 3");

            mouseMoveAndClickAndHide(p);
            Log.debug("Success on last click");
            return true;
        } finally {
            sc.flush();
        }
    }

    protected boolean clickImageScanBW(BwMatrixMeta im) {
        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im);
        BufferedImage sc = screenCapturedResult.image;

        try {
            saveDebugImage(sc, "clickImageScanBW");

            boolean go = true;
            Point p = new Point();
            int blackPixelRgb = im.getBlackPixelRgb();
            ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int y = 0; y < sc.getHeight() - im.getHeight() && go; y++) {
                for (int x = 0; x < sc.getWidth() - im.getWidth() && go; x++) {
                    int rgb = sc.getRGB(x, y) & 0xFFFFFF;
                    if (!im.isMatchBlackRgb(rgb)) {
                        continue;
                    }

                    // debug(String.format("clickImageScanBW first match passed for %d,%d", x, y));
                    boolean allGood = true;

                    for (int[] px : im.getBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (!ImageUtil.areColorsSimilar(//
                                blackPixelDRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            // debug(String.format("clickImageScanBW second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (allGood) {
                        // debug("clickImageScanBW second match passed");
                        for (int[] px : im.getNonBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (ImageUtil.areColorsSimilar(//
                                    blackPixelRgb, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                // debug(String.format("clickImageScanBW third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                break;
                            }
                        }
                    }

                    if (allGood) {
                        // debug("clickImageScanBW third match passed");
                        go = false;
                        p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                    }
                }
            }

            if (!go) {
                mouseMoveAndClickAndHide(p);
                im.setLastMatchPoint(p.x, p.y);
                return true;
            }

            return false;
        } finally {
            sc.flush();
        }
    }

    protected Point detectLabel(BwMatrixMeta im, int... mainColors) {
        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im);
        BufferedImage sc = screenCapturedResult.image;
        try {
            saveDebugImage(sc, "detectLabel");

            for (int y = 0; y < sc.getHeight() - im.getHeight(); y++) {
                for (int x = 0; x < sc.getWidth() - im.getWidth(); x++) {
                    for (int mainColor : mainColors) {
                        final int c = mainColor & 0xFFFFFF;
                        final ImageUtil.DynamicRgb cDRgb = new ImageUtil.DynamicRgb(c, Configuration.Tolerant.colorBw);

                        boolean allGood = true;

                        for (int[] px : im.getBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (!ImageUtil.areColorsSimilar(//
                                    cDRgb, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                // debug(String.format("detectLabel second match failed at %d,%d (%d,%d), %s vs %s", x + px[0], y + px[1], px[0], px[1], String.format("%06X", c), String.format("%06X", srcRgb)));
                                break;
                            }
                        }

                        if (!allGood)
                            continue;

                        Log.debug("detectLabel second match passed");
                        for (int[] px : im.getNonBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (ImageUtil.areColorsSimilar(//
                                    c, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                // debug(String.format("detectLabel third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                                break;
                            }
                        }

                        if (!allGood)
                            continue;

                        // debug("detectLabel third match passed");
                        debug("detectLabel captured at %3d,%3d with size %3dx%3d, match at %3d,%3d", screenCapturedResult.x, screenCapturedResult.y, screenCapturedResult.w, screenCapturedResult.h, x, y);
                        Point result = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                        im.setLastMatchPoint(result.x, result.y);
                        return result;
                    }
                }
            }

            return null;
        } finally {
            sc.flush();
        }
    }

    protected Point detectLabelFishing() {
        return detectLabel(BwMatrixMeta.Metas.Fishing.Labels.fishing, 0xFFFFFF, 0x7F7F7F);
    }

    protected void doClickTalk(Supplier<Boolean> shouldStop) {
        int sleepSecs = 60;
        int sleepSecsWhenClicked = 3;
        int cnt = sleepSecs;
        while (!shouldStop.get()) {
            cnt--;
            ThreadUtil.sleep(1000);
            if (cnt > 0) {
                continue;
            }

            cnt = sleepSecs;
            if (clickImage(BwMatrixMeta.Metas.Globally.Buttons.talkRightArrow)) {
                Log.debug("clicked talk");
                cnt = sleepSecsWhenClicked;
            } else {
                Log.debug("No talk");
            }
        }
    }

    protected void detectDisconnected(AtomicBoolean masterSwitch) {
        int sleepSecs = 60;
        int cnt = sleepSecs;
        while (!masterSwitch.get()) {
            cnt--;
            ThreadUtil.sleep(1000);
            if (cnt > 0) {
                continue;
            }

            cnt = sleepSecs;
            if (clickImage(ImgMeta.Metas.Globally.Buttons.reconnect)) {
                masterSwitch.set(true);
                Telegram.sendMessage("Disconnected", true);
            }
        }
    }

    protected void autoExit(int exitAfterXSecs, AtomicBoolean masterSwitch) {
        if (exitAfterXSecs < 1)
            return;
        while (exitAfterXSecs > 0) {
            exitAfterXSecs--;
            ThreadUtil.sleep(1000);
            if (exitAfterXSecs % 60 == 0)
                Log.info("Exit after %d seconds", exitAfterXSecs);
            if (masterSwitch.get())
                break;
        }
        masterSwitch.set(true);
        Log.info("Application is going to exit now");
    }

    protected void throwNotSupportedFlagExit(int exitAfterXSecs) {
        if (exitAfterXSecs > 0)
            throw new IllegalArgumentException(String.format("Flag --exit does not supported by this application"));
    }

    protected <T> T readInput(String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform) {
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                while (true) {
                    Log.info(ask);
                    if (desc != null)
                        Log.info("(%s)", desc);
                    input = br.readLine();
                    if (isBlank(input)) {
                        Log.info("You inputted nothing, please try again!");
                        continue;
                    }

                    Tuple3<Boolean, String, T> tuple = transform.apply(input);
                    if (!tuple._1) {
                        info(tuple._2);
                        Log.info("Please try again!");
                        continue;
                    }

                    return tuple._3;
                }
            }
        } catch (IOException e) {
            Log.err("Error while reading input, application is going to exit now, please try again later");
            System.exit(3);
            return null;
        }
    }
}
