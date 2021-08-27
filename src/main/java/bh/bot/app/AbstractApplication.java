package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.flags.FlagPattern;
import bh.bot.common.types.flags.Flags;
import bh.bot.common.types.images.BwMatrixMeta;
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
    protected ParseArgumentsResult argumentInfo;
    private final String appCode = this.getClass().getAnnotation(AppCode.class).code();

    public void run(ParseArgumentsResult launchInfo) throws Exception {
        this.argumentInfo = launchInfo;
        if (Configuration.screenResolutionProfile instanceof ScreenResolutionProfile.SteamProfile && !isSupportSteamScreenResolution()) {
            err("'%s' does not support steam resolution");
            System.exit(Main.EXIT_CODE_SCREEN_RESOLUTION_ISSUE);
            return;
        }

        if (this.argumentInfo.enableSavingDebugImages)
            info("Enabled saving debug images");
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
        if (!this.argumentInfo.enableSavingDebugImages) {
            return;
        }
        File file = Paths.get("out", "images", getAppCode(), "dbg_" + prefix + "_" + System.currentTimeMillis() + ".bmp").toFile();
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
            err("Unable to save image file '%s' to %s", file.getName(), file.toString());
        }
    }

    protected void saveImage(BufferedImage img, String prefix) {
        File file = Paths.get("out", "images", getAppCode(), prefix + "_" + System.currentTimeMillis() + ".bmp").toFile();
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
            err("Unable to save image file '%s' to %s", file.getName(), file.toString());
        }
    }

    protected abstract void internalRun(String[] args);

    public final String getAppCode() {
        return this.appCode;
    }

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

        List<FlagPattern> flagPatterns = Arrays.asList(Flags.allFlags);
        // Local flags
        List<FlagPattern> localFlags = flagPatterns.stream()
                .filter(x -> !x.isGlobalFlag())
                .filter(x -> x.isSupportedByApp(this))
                .collect(Collectors.toList());
        if (localFlags.size() > 0) {
            sb.append("\nFlags:");
            for (FlagPattern localFlag : localFlags)
                sb.append(String.format("\n  --%s%s : %s%s", localFlag.getName(), localFlag.isAllowParam() ? "=?" : "", localFlag.isDevelopersOnly() ? "(developers only) " : "", localFlag.getDescription()));
        }
        // Global flags:
        List<FlagPattern> globalFlags = flagPatterns.stream()
                .filter(x -> x.isGlobalFlag())
                .collect(Collectors.toList());
        sb.append("\nGlobal flags:");
        for (FlagPattern globalFlag : globalFlags)
            sb.append(String.format("\n  --%s%s : %s%s", globalFlag.getName(), globalFlag.isAllowParam() ? "=?" : "", globalFlag.isDevelopersOnly() ? "(developers only) " : "", globalFlag.getDescription()));
        if (!this.isSupportSteamScreenResolution())
            sb.append("\n** WARNING ** This app does not supports '--steam' flag");
        return sb.toString();
    }

    protected abstract String getLimitationExplain();

    protected boolean isSupportSteamScreenResolution() {
        return false;
    }

    protected boolean clickImage(BwMatrixMeta im) {
        return clickImageExact(im) || clickImageScanBW(im);
    }

    protected boolean clickImageExact(BwMatrixMeta im) {
        int[] lastMatch = im.getLastMatchPoint();
        if (lastMatch[0] < 0 || lastMatch[1] < 0) {
            return false;
        }

        int[] firstBlackPixelOffset = im.getFirstBlackPixelOffset();
        Point p = new Point(lastMatch[0] + firstBlackPixelOffset[0], lastMatch[1] + firstBlackPixelOffset[1]);
        Color c = getPixelColor(p);
        if (!im.isMatchBlackRgb(c.getRGB())) {
            return false;
        }
        // debug("clickImageExactBW match success 1");

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
            // debug("Success on last click");
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
                            // debug(String.format("clickImageScanBW second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

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

                    if (!allGood)
                        continue;

                    // debug("clickImageScanBW third match passed");
                    go = false;
                    p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                }
            }

            if (!go) {
                mouseMoveAndClickAndHide(p);
                im.setLastMatchPoint(p.x, p.y);
                printIfIncorrectImgPosition(im.getCoordinateOffset(), p);
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

                        // debug("detectLabel second match passed");
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
                debug("clicked talk");
                cnt = sleepSecsWhenClicked;
            } else {
                debug("No talk");
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
                info("Exit after %d seconds", exitAfterXSecs);
            if (masterSwitch.get())
                break;
        }
        masterSwitch.set(true);
        info("Application is going to exit now");
    }

    protected void throwNotSupportedFlagExit(int exitAfterXSecs) {
        if (exitAfterXSecs > 0)
            throw new IllegalArgumentException(String.format("Flag --exit does not supported by this application"));
    }

    protected <T> T readInput(BufferedReader br, String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform) {
        return readInput(br, ask, desc, transform, false);
    }

    protected <T> T readInput(BufferedReader br, String ask, String desc, Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
        return readInput(br, ask, desc, null, transform, allowBlankAndIfBlankThenReturnNull);
    }

    protected <T> T readInput(BufferedReader br, String ask, String desc, Supplier<List<String>> selectedOptionsInfoProvider, Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
        try {
            String input;
            while (true) {
                info(ask);
                if (desc != null)
                    info("(%s)", desc);
                if (selectedOptionsInfoProvider != null) {
                    List<String> selectedOptions = selectedOptionsInfoProvider.get();
                    if (selectedOptions.size() > 0)
                        info("Selected: %s", String.join(", ", selectedOptions));
                }
                input = br.readLine();

                if (isBlank(input)) {
                    if (allowBlankAndIfBlankThenReturnNull)
                        return null;
                    info("You inputted nothing, please try again!");
                    continue;
                }

                Tuple3<Boolean, String, T> tuple = transform.apply(input);
                if (!tuple._1) {
                    info(tuple._2);
                    info("Please try again!");
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
