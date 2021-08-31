package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.flags.FlagPattern;
import bh.bot.common.types.flags.Flags;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.InteractionUtil.Screen.*;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.ThreadUtil.sleep;

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
        if (isRequiredToLoadImages())
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

    public String getHelp() {
        StringBuilder sb = new StringBuilder(getAppName());
        sb.append("\n  Description: ");
        sb.append(getDescription());
        sb.append("\nUsage:\n");
        if (Configuration.OS.isWin) {
            sb.append("java -jar BitHeroes.jar ");
            sb.append(getAppCode());
        } else
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
                sb.append(localFlag);
        }
        // Global flags:
        List<FlagPattern> globalFlags = flagPatterns.stream()
                .filter(x -> x.isGlobalFlag())
                .collect(Collectors.toList());
        sb.append("\nGlobal flags:");
        for (FlagPattern globalFlag : globalFlags)
            sb.append(globalFlag);
        if (!this.isSupportSteamScreenResolution())
            sb.append("\n** WARNING ** This app does not supports '--steam' flag");
        return sb.toString();
    }

    protected abstract String getLimitationExplain();

    protected boolean isSupportSteamScreenResolution() {
        return false;
    }

    protected boolean isRequiredToLoadImages() {
        return true;
    }

    protected boolean clickImage(BwMatrixMeta im) {
        Point p = findImageBasedOnLastClick(im);
        if (p != null) {
            mouseMoveAndClickAndHide(p);
            return true;
        }
        return clickImageScanBW(im);
    }

    protected Point findImageBasedOnLastClick(BwMatrixMeta im) {
        im.throwIfNotAvailable();

        int[] lastMatch = im.getLastMatchPoint();
        if (lastMatch[0] < 0 || lastMatch[1] < 0) {
            return null;
        }

        int[] firstBlackPixelOffset = im.getFirstBlackPixelOffset();
        Point p = new Point(lastMatch[0] + firstBlackPixelOffset[0], lastMatch[1] + firstBlackPixelOffset[1]);
        Color c = getPixelColor(p);
        if (!im.isMatchBlackRgb(c.getRGB()))
            return null;
        debug("findImageBasedOnLastClick match success 1 for %s", im.getImageNameCode());

        BufferedImage sc = captureScreen(lastMatch[0], lastMatch[1], im.getWidth(), im.getHeight());

        try {
            final int blackPixelRgb = im.getBlackPixelRgb();
            final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int[] px : im.getBlackPixels()) {
                if (!ImageUtil.areColorsSimilar(//
                        blackPixelDRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return null;
                }
            }

            // debug("findImageBasedOnLastClick match success 2");

            for (int[] px : im.getNonBlackPixels()) {
                if (ImageUtil.areColorsSimilar(//
                        blackPixelRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return null;
                }
            }

            // debug("findImageBasedOnLastClick match success (result)");
            return p;
        } finally {
            freeMem(sc);
        }
    }

    protected Point scanToFindImage(BwMatrixMeta im) {
        im.throwIfNotAvailable();

        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im);
        BufferedImage sc = screenCapturedResult.image;

        try {
            saveDebugImage(sc, "scanToFindImage");

            boolean go = true;
            Point p = new Point();
            final int blackPixelRgb = im.getBlackPixelRgb();
            final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int y = sc.getHeight() - im.getHeight() - 1; y >= 0 && go; y--) {
                for (int x = sc.getWidth() - im.getWidth() - 1; x >= 0 && go; x--) {
                    boolean allGood = true;

                    for (int[] px : im.getBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (!ImageUtil.areColorsSimilar(//
                                blackPixelDRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            // debug(String.format("scanToFindImage second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    // debug("scanToFindImage second match passed");
                    for (int[] px : im.getNonBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (ImageUtil.areColorsSimilar(//
                                blackPixelRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            // debug(String.format("scanToFindImage third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    // debug("scanToFindImage third match passed");
                    go = false;
                    p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                }
            }

            if (!go)
                return p;

            return null;
        } finally {
            freeMem(sc);
        }
    }

    protected Point findImage(BwMatrixMeta im) {
        Point result = findImageBasedOnLastClick(im);
        if (result != null)
            return result;
        return scanToFindImage(im);
    }

    protected boolean clickImageScanBW(BwMatrixMeta im) {
        Point p = scanToFindImage(im);
        if (p == null)
            return false;
        mouseMoveAndClickAndHide(p);
        im.setLastMatchPoint(p.x, p.y);
        printIfIncorrectImgPosition(im.getCoordinateOffset(), p);
        return true;
    }

    protected Point detectLabel(BwMatrixMeta im, int... mainColors) {
        im.throwIfNotAvailable();

        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im);
        BufferedImage sc = screenCapturedResult.image;
        try {
            saveDebugImage(sc, "detectLabel");

            for (int y = sc.getHeight() - im.getHeight() - 1; y >= 0; y--) {
                for (int x = sc.getWidth() - im.getWidth() - 1; x >= 0; x--) {
                    for (int mainColor : mainColors) {
                        final int c = mainColor & 0xFFFFFF;
                        final ImageUtil.DynamicRgb cDRgb = new ImageUtil.DynamicRgb(c, im.getTolerant());

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
            freeMem(sc);
        }
    }

    protected Point detectLabelFishing() {
        return detectLabel(BwMatrixMeta.Metas.Fishing.Labels.fishing, 0xFFFFFF, 0x7F7F7F);
    }

    private final byte greenMinDiff = 70;

    protected Tuple2<Point[], Byte> detectRadioButtons(Rectangle scanRect) {
        final BwMatrixMeta im = BwMatrixMeta.Metas.Globally.Buttons.radioButton;
        im.throwIfNotAvailable();

        int positionTolerant = Math.min(10, Configuration.Tolerant.position);
        ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(
                new Configuration.Offset(
                        Math.max(0, scanRect.x - positionTolerant),
                        Math.max(0, scanRect.y - positionTolerant)
                ),
                scanRect.width + positionTolerant * 2,
                scanRect.height + positionTolerant * 2
        );
        BufferedImage sc = screenCapturedResult.image;
        try {
            saveDebugImage(sc, "detectRadioButtons");

            ArrayList<Point> startingCoord = new ArrayList<>();
            int selectedRadioButtonIndex = -1;
            int skipAfterXIfNotFoundAny = (int) Math.floor((double) sc.getWidth() / 4 * 3);

            for (int y = 0; y < sc.getHeight() - im.getHeight() && startingCoord.size() < 1; y++) {
                for (int x = 0; x < sc.getWidth() - im.getWidth(); x++) {
                    if (x >= skipAfterXIfNotFoundAny && startingCoord.size() == 0)
                        break;

                    final int blackPixelRgb = im.getBlackPixelRgb();
                    ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();

                    boolean allGood = true;

                    for (int[] px : im.getBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (!ImageUtil.areColorsSimilar(//
                                blackPixelDRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            // debug(String.format("detectRadioButtons second match failed at %d,%d (%d,%d), %s vs %s", x + px[0], y + px[1], px[0], px[1], String.format("%06X", blackPixelRgb), String.format("%06X", srcRgb)));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    // debug("detectRadioButtons second match passed");
                    for (int[] px : im.getNonBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (ImageUtil.areColorsSimilar(//
                                blackPixelRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            // debug(String.format("detectRadioButtons third match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    // detect selected button index
                    for (int[] px : im.getNonBlackPixels()) {
                        int rgb = sc.getRGB(x + px[0], y + px[1]);
                        int green = ImageUtil.getGreen(rgb);
                        if (green <= greenMinDiff)
                            continue;
                        int red = ImageUtil.getRed(rgb);
                        if (red > green - greenMinDiff)
                            continue;
                        int blue = ImageUtil.getBlue(rgb);
                        if (blue > green - greenMinDiff)
                            continue;

                        int curRadioButtonIndex = startingCoord.size();
                        if (selectedRadioButtonIndex < 0)
                            selectedRadioButtonIndex = curRadioButtonIndex;
                        else {
                            if (selectedRadioButtonIndex != curRadioButtonIndex)
                                throw new InvalidDataException("Found more than one selected radio button which is absolutely wrong!");
                        }
                        break;
                    }

                    // debug("detectRadioButtons captured at %3d,%3d with size %3dx%3d, match at %3d,%3d", screenCapturedResult.x, screenCapturedResult.y, screenCapturedResult.w, screenCapturedResult.h, x, y);
                    // im.setLastMatchPoint(startingCoord.x, startingCoord.y);
                    startingCoord.add(new Point(x, y));
                }
            }

            if (selectedRadioButtonIndex < 0)
                throw new InvalidDataException("Unable to detect index of selected radio button among %d results", startingCoord.size());

            return new Tuple2<>(
                    startingCoord
                            .stream()
                            .map(c ->
                                    new Point(
                                            screenCapturedResult.x + c.x,
                                            screenCapturedResult.y + c.y
                                    )
                            )
                            .collect(Collectors.toList())
                            .toArray(new Point[0]),
                    (byte) selectedRadioButtonIndex
            );
        } finally {
            freeMem(sc);
        }
    }

    protected void doClickTalk(Supplier<Boolean> shouldStop) {
        try {
            int sleepSecs = 60;
            int sleepSecsWhenClicked = 3;
            int cnt = sleepSecs;
            while (!shouldStop.get()) {
                cnt--;
                sleep(1000);
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
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
        }
    }

    protected void detectDisconnected(AtomicBoolean masterSwitch) {
        try {
            int sleepSecs = 60;
            int cnt = sleepSecs;
            while (!masterSwitch.get()) {
                cnt--;
                sleep(1000);
                if (cnt > 0) {
                    continue;
                }

                cnt = sleepSecs;
                if (clickImage(BwMatrixMeta.Metas.Globally.Buttons.reconnect)) {
                    masterSwitch.set(true);
                    Telegram.sendMessage("Disconnected", true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    protected void autoReactiveAuto(AtomicBoolean masterSwitch) {
        try {
            final int sleepMs = 10_000;
            int continousRed = 0;
            final int maxContinousRed = 6;
            while (!masterSwitch.get()) {
                sleep(sleepMs);
                Point point = findImage(BwMatrixMeta.Metas.Globally.Buttons.autoG);
                if (point == null) {
                    debug("AutoG button not found");
                    point = findImage(BwMatrixMeta.Metas.Globally.Buttons.autoR);
                    if (point == null) {
                        debug("AutoR button not found");
                        continue;
                    }
                }
                debug("Found the Auto button at %d,%d", point.x, point.y);
                Color color = getPixelColor(point.x - 5, point.y);
                if (ImageUtil.isGreenLikeColor(color)) {
                    debug("Auto is currently ON (green)");
                    continousRed = 0;
                    continue;
                }
                if (ImageUtil.isRedLikeColor(color)) {
                    continousRed++;
                    if (continousRed >= 2)
                        info("Detected Auto is not turned on, gonna reactive it soon");
                    if (continousRed >= maxContinousRed) {
                        moveCursor(point);
                        sleep(100);
                        mouseClick();
                        hideCursor();

                        info("Sent re-active");
                        sleep(2_000);
                    }
                } else {
                    debug("Red Auto not found");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
    }

    protected void autoExit(int exitAfterXSecs, AtomicBoolean masterSwitch) {
        try {
            if (exitAfterXSecs < 1)
                return;
            while (exitAfterXSecs > 0) {
                exitAfterXSecs--;
                sleep(1000);
                if (exitAfterXSecs % 60 == 0)
                    info("Exit after %d seconds", exitAfterXSecs);
                if (masterSwitch.get())
                    break;
            }
            masterSwitch.set(true);
            info("Application is going to exit now");
        } catch (Exception ex) {
            ex.printStackTrace();
            Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
            masterSwitch.set(true);
        }
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
                if (selectedOptionsInfoProvider != null) {
                    List<String> selectedOptions = selectedOptionsInfoProvider.get();
                    if (selectedOptions.size() > 0)
                        info("Selected: %s", String.join(", ", selectedOptions));
                }
                if (desc != null)
                    info("(%s)", desc);
                info("** Notice ** Please complete the above question first, otherwise bot will be hanged here!!!");
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
