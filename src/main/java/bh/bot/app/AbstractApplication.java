package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.Telegram;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.images.ImgMeta;
import bh.bot.common.types.images.Pixel;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.ThreadUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
    private static List<String> flags;

    public static LaunchInfo parse(String[] args) {
        String appCode = args[0];

        flags = Arrays
                .stream(args)
                .map(x -> x.toLowerCase().trim())
                .filter(x -> x.startsWith("--"))
                .collect(Collectors.toList());

        if (hasFlag("--debug"))
            Log.enableDebug();
        if (hasFlag("--mute"))
            Telegram.disable();
        else if (Telegram.isDisabled())
            Log.info("Telegram was disabled due to missing or invalid configuration");

        if (hasFlag("--exit"))
            err("Invalid usage of flag 'exit', should be '--exit=X' where X is the number of seconds to await before force exit, for example: '--exit=3600' means exit after 1 hours");

        List<String> exitCmd = flags.stream().filter(x -> x.startsWith("--exit=") && x.length() > 7).collect(Collectors.toList());
        int exitAfter = 0;
        if (exitCmd.size() > 0) {
            String sExitAfter = exitCmd.get(0).substring(7);
            try {
                exitAfter = Math.max(0, Integer.parseInt(sExitAfter));
                if (exitAfter > 3600) {
                    int h = exitAfter / 3600;
                    int m = (exitAfter - h * 3600) / 60;
                    info("Application will exit after %d hours and %d minutes", h, m);
                }
            } catch (NumberFormatException e) {
                err("Can not parse command: --exit=%s", sExitAfter);
            }
        }

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
        li.displayHelp = hasFlag("--help");
        li.enableSavingDebugImages = hasFlag("--img");
        // events
        li.eInvasion = hasFlag("--invasion");
        li.eTrials = hasFlag("--trials") || hasFlag("--trial");
        li.ePvp = hasFlag("--pvp");
        li.eWorldBoss = hasFlag("--boss") || hasFlag("--worldboss") || hasFlag("--world-boss");
        li.eRaid = hasFlag("--raid") || hasFlag("--raids");
        // end events
        flags.clear();
        return li;
    }

    private static boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public static class LaunchInfo {
        public AbstractApplication instance;
        public String[] arguments;
        public int exitAfterXSecs;
        public boolean displayHelp;
        public boolean enableSavingDebugImages;
        public boolean eInvasion;
        public boolean eTrials;
        public boolean ePvp;
        public boolean eWorldBoss;
        public boolean eRaid;

        public LaunchInfo(AbstractApplication instance, String[] args) {
            this.instance = instance;
            this.arguments = args;
        }
    }

    protected LaunchInfo launchInfo;

    public void run(LaunchInfo launchInfo) throws Exception {
        this.launchInfo = launchInfo;
        if (Configuration.screenResolutionProfile instanceof ScreenResolutionProfile.SteamProfile && !isSupportSteamScreenResolution()) {
            err("'%s' does not support steam resolution");
            System.exit(Main.EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
            return;
        }

        if (this.launchInfo.enableSavingDebugImages)
            Log.info("Enabled saving debug images");
        initOutputDirectories();
        // ImgMeta.load(); // Deprecated class
        BwMatrixMeta.load();
        Telegram.setAppName(getAppName());
        warn(getLimitationExplain());
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

    public void saveDebugImage(BufferedImage img, String prefix) {
        if (!this.launchInfo.enableSavingDebugImages) {
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
        if (Configuration.OS.isWin)
            sb.append("java -jar BitHeroes.jar");
        else
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
        if (this.isSupportSteamScreenResolution()) {
            sb.append("\n  --web : use mode resolution 800x520 (default)");
            sb.append("\n  --steam : use mode resolution 800x480 (Steam client)");
        }
        return sb.toString();
    }

    protected abstract String getLimitationExplain();

    protected boolean isSupportSteamScreenResolution() {
        return false;
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

    @Deprecated
    protected boolean clickImage(ImgMeta im) {
        return clickImageExact(im) || clickImageScanBW(im);
    }

    @Deprecated
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
        debug("clickImageExactBW match success 1");

        BufferedImage sc = captureScreen(lastMatch[0], lastMatch[1], im.getWidth(), im.getHeight());

        try {
            final int blackPixelRgb = im.getBlackPixelRgb();
            final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int[] px : im.getBlackPixels()) {
                if (!ImageUtil.areColorsSimilar(//
                        blackPixelDRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return false;
                }
            }

            debug("clickImageExactBW match success 2");

            for (int[] px : im.getNonBlackPixels()) {
                if (ImageUtil.areColorsSimilar(//
                        blackPixelRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return false;
                }
            }

            debug("clickImageExactBW match success 3");

            mouseMoveAndClickAndHide(p);
            debug("Success on last click");
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
                            debug(String.format("clickImageScanBW second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    debug("clickImageScanBW second match passed");
                    for (int[] px : im.getNonBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (ImageUtil.areColorsSimilar(//
                                blackPixelRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            debug(String.format("clickImageScanBW third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    debug("clickImageScanBW third match passed");
                    go = false;
                    p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
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

                        debug("detectLabel second match passed");
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
            if (clickImage(BwMatrixMeta.Metas.Globally.Buttons.reconnect)) {
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

    protected <T> T readInput(BufferedReader br, String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform) {
        return readInput(br, ask, desc, transform, false);
    }

    protected <T> T readInput(BufferedReader br, String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
        try {
            String input;
            while (true) {
                Log.info(ask);
                if (desc != null)
                    Log.info("(%s)", desc);
                input = br.readLine();

                if (isBlank(input)) {
                    if (allowBlankAndIfBlankThenReturnNull)
                        return null;
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
        } catch (IOException e) {
            e.printStackTrace();
            err("Error while reading input, application is going to exit now, please try again later");
            System.exit(Main.EXIT_CODE_FAILURE_READING_INPUT);
            return null;
        }
    }
}
