package bh.bot.app;

import bh.bot.Main;
import bh.bot.app.farming.ExpeditionApp.ExpeditionPlace;
import bh.bot.common.Configuration;
import bh.bot.common.OS;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.jna.*;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.UserConfig;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.flags.FlagPattern;
import bh.bot.common.types.flags.FlagResolution;
import bh.bot.common.types.flags.FlagShutdownAfterFinished;
import bh.bot.common.types.flags.Flags;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.InteractionUtil.Screen.*;
import com.sun.jna.platform.win32.WinDef.HWND;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.*;
import static bh.bot.common.utils.Extensions.scriptFileName;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.InteractionUtil.Screen.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

public abstract class AbstractApplication {
    protected ParseArgumentsResult argumentInfo;
    private final String appCode = this.getClass().getAnnotation(AppMeta.class).code();

    public void run(ParseArgumentsResult launchInfo) throws Exception {
        this.argumentInfo = launchInfo;

        if (this.argumentInfo.enableSavingDebugImages)
            info("Enabled saving debug images");
        initOutputDirectories();
        // ImgMeta.load(); // Deprecated class
        if (isRequiredToLoadImages())
            BwMatrixMeta.load();
        Telegram.setAppName(getAppName());
        warn(getLimitationExplain());

        if (launchInfo.shutdownAfterFinished) {
            String command;
            if (OS.isWin)
                command = "shutdown -s -t 0";
            else if (OS.isLinux) {
                command = "sudo shutdown now";
                try {
                    if (0 != Runtime.getRuntime().exec("sudo -nv").waitFor()) {
                        err("You must run this bot as sudo to be able to run command '%s' upon completion", command);
                        System.exit(Main.EXIT_CODE_REQUIRE_SUDO);
                    }
                } catch (Exception ex) {
                    err(ex.getMessage());
                    err("Unable to check compatible between `--shutdown` flag and your system, not sure if it works");
                }
            } else
                throw new NotSupportedException(String.format("Shutdown command is not supported on %s OS", OS.name));

            internalRun(launchInfo.arguments);

            final int shutdownAfterMinutes = FlagShutdownAfterFinished.shutdownAfterXMinutes;
            final int notiEverySec = 30;
            warn("System is going to shutdown after %d minutes", shutdownAfterMinutes);
            final int sleepPerRound = notiEverySec * 1_000;
            int count = shutdownAfterMinutes * 60_000 / sleepPerRound;
            while (count-- > 0) {
                sleep(sleepPerRound);
                warn("System is going to shutdown after %d seconds", count * sleepPerRound / 1_000);
            }
            warn("System is going to shutdown NOW");

            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception ex) {
                ex.printStackTrace();
                err("Error occurs while trying to shutdown system");
            }
        } else {
            internalRun(launchInfo.arguments);
        }
    }

    private void initOutputDirectories() {
        mkdir("out");
        mkdir("out", "images");
        mkdir("out", "images", getAppCode());
    }

    @SuppressWarnings("SameParameterValue")
    private void mkdir(String path, String... paths) {
        File file = Paths.get(path, paths).toFile();
        if (!file.exists())
            //noinspection ResultOfMethodCallIgnored
            file.mkdir();
    }

    protected BufferedImage loadImageFromFile(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public void saveDebugImage(BufferedImage img, String prefix) {
        if (!this.argumentInfo.enableSavingDebugImages) {
            return;
        }
        File file = Paths
                .get("out", "images", getAppCode(), "dbg_" + prefix + "_" + System.currentTimeMillis() + ".bmp")
                .toFile();
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
            err("Unable to save image file '%s' to %s", file.getName(), file.toString());
        }
    }

    protected void saveImage(BufferedImage img, String prefix) {
        File file = Paths.get("out", "images", getAppCode(), prefix + "_" + System.currentTimeMillis() + ".bmp")
                .toFile();
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

    protected final String getAppName() {
        return this.getClass().getAnnotation(AppMeta.class).name();
    }

    protected abstract String getUsage();

    protected abstract String getDescription();

    @SuppressWarnings("rawtypes")
    public String getHelp() {
        StringBuilder sb = new StringBuilder(getAppName());
        sb.append("\n  Description: ");
        sb.append(getDescription());
        sb.append("\nUsage:\n");
        if (OS.isWin)
            sb.append("java -jar BitHeroes.jar ");
        else
            sb.append("./bot.sh ");
        sb.append(getAppCode());
        String usage = getUsage();
        if (usage != null) {
            sb.append(' ');
            sb.append(usage);
        }
        sb.append(" [additional flags]");

        List<FlagPattern> flagPatterns = Arrays.asList(Flags.allFlags);
        // Local flags
        List<FlagPattern> localFlags = flagPatterns.stream()
                .filter(x -> !x.isGlobalFlag() && x.isSupportedByApp(this))
                .collect(Collectors.toList());
        if (localFlags.size() > 0) {
            sb.append("\nFlags:");
            for (FlagPattern localFlag : localFlags)
                sb.append(localFlag);
        }
        // Global flags:
        List<FlagPattern> globalFlags = flagPatterns.stream()
                .filter(FlagPattern::isGlobalFlag)
                .filter(x -> x.isSupportedByApp(this))
                .collect(Collectors.toList());
        sb.append("\nGlobal flags:");
        for (FlagPattern globalFlag : globalFlags.stream().filter(x -> !(x instanceof FlagResolution))
                .collect(Collectors.toList()))
            sb.append(globalFlag);
        List<FlagPattern> flagsResolution = globalFlags.stream().filter(x -> x instanceof FlagResolution)
                .collect(Collectors.toList());
        if (flagsResolution.size() > 0) {
            for (FlagPattern globalFlag : flagsResolution)
                sb.append(globalFlag);
        }
        return sb.toString();
    }

    protected abstract String getLimitationExplain();

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
        if (im.throwIfNotAvailable())
            return null;

        int[] lastMatch = im.getLastMatchPoint();
        if (lastMatch[0] < 0 || lastMatch[1] < 0) {
            return null;
        }

        int[] firstBlackPixelOffset = im.getFirstBlackPixelOffset();
        Point p = new Point(lastMatch[0] + firstBlackPixelOffset[0], lastMatch[1] + firstBlackPixelOffset[1]);
        Color c = getPixelColor(p);
        if (!im.isMatchBlackRgb(c.getRGB()))
            return null;

        final boolean debug = false;
        optionalDebug(debug, "findImageBasedOnLastClick match success 1 for %s", im.getImageNameCode());

        BufferedImage sc = captureScreen(lastMatch[0], lastMatch[1], im.getWidth(), im.getHeight());

        try {
            final int blackPixelRgb = im.getBlackPixelRgb();
            final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int[] px : im.getBlackPixels()) {
                if (!ImageUtil.areColorsSimilar(//
                        blackPixelDRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color, im.getOriginalPixelPart(px[0], px[1]))) {
                    return null;
                }
            }

            optionalDebug(debug, "findImageBasedOnLastClick match success 2");

            for (int[] px : im.getNonBlackPixels()) {
                if (ImageUtil.areColorsSimilar(//
                        blackPixelRgb, //
                        sc.getRGB(px[0], px[1]) & 0xFFFFFF, //
                        Configuration.Tolerant.color)) {
                    return null;
                }
            }

            optionalDebug(debug, "findImageBasedOnLastClick result %d,%d", p.x, p.y);
            return p;
        } finally {
            freeMem(sc);
        }
    }

    protected Point scanToFindImage(BwMatrixMeta im) {
        if (im.throwIfNotAvailable())
            return null;

        // final boolean debug = im == BwMatrixMeta.Metas.Raid.Buttons.accept;
        final boolean debug = false;

        try (ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im)) {
            BufferedImage sc = screenCapturedResult.image;
            saveDebugImage(sc, "scanToFindImage." + im.getImageNameCode());

            boolean go = true;
            Point p = new Point();
            final int blackPixelRgb = im.getBlackPixelRgb();
            final ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();
            for (int y = sc.getHeight() - im.getHeight() - 1; y >= 0 && go; y--) {
                for (int x = sc.getWidth() - im.getWidth() - 1; x >= 0 && go; x--) {
                    boolean allGood = true;

                    for (int[] px : im.getBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        Short originalPixelPart = im.getOriginalPixelPart(px[0], px[1]);
                        if (!ImageUtil.areColorsSimilar(//
                                blackPixelDRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color, originalPixelPart)) {
                            allGood = false;
                            optionalDebug(debug,
                                    "scanToFindImage first match failed at %d,%d (%d,%d) and original = %s", x + px[0],
                                    y + px[1], px[0], px[1],
                                    originalPixelPart == null ? "null" : String.valueOf(originalPixelPart.byteValue()));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    for (int[] px : im.getNonBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (ImageUtil.areColorsSimilar(//
                                blackPixelRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            optionalDebug(debug, "scanToFindImage second match failed at %d,%d (%d,%d)", x + px[0],
                                    y + px[1], px[0], px[1]);
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    go = false;
                    p = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                    optionalDebug(debug, "scanToFindImage result %d,%d", p.x, p.y);
                }
            }

            if (!go)
                return p;

            return null;
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
        if (im.throwIfNotAvailable())
            return null;

        final boolean debug = false;

        try (ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im)) {
            BufferedImage sc = screenCapturedResult.image;
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
                                    Configuration.Tolerant.color, im.getOriginalPixelPart(px[0], px[1]))) {
                                allGood = false;
                                optionalDebug(debug, "detectLabel second match failed at %d,%d (%d,%d), %s vs %s",
                                        x + px[0], y + px[1], px[0], px[1], String.format("%06X", c),
                                        String.format("%06X", srcRgb));
                                break;
                            }
                        }

                        if (!allGood)
                            continue;

                        for (int[] px : im.getNonBlackPixels()) {
                            int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                            if (ImageUtil.areColorsSimilar(//
                                    c, //
                                    srcRgb, //
                                    Configuration.Tolerant.color)) {
                                allGood = false;
                                optionalDebug(debug, "detectLabel third match failed at %d,%d (%d,%d)", x + px[0],
                                        y + px[1], px[0], px[1]);
                                break;
                            }
                        }

                        if (!allGood)
                            continue;

                        optionalDebug(debug, "detectLabel captured at %3d,%3d with size %3dx%3d, match at %3d,%3d",
                                screenCapturedResult.x, screenCapturedResult.y, screenCapturedResult.w,
                                screenCapturedResult.h, x, y);
                        Point result = new Point(screenCapturedResult.x + x, screenCapturedResult.y + y);
                        im.setLastMatchPoint(result.x, result.y);
                        return result;
                    }
                }
            }

            return null;
        }
    }

    protected Point detectLabelFishing() {
        return detectLabel(BwMatrixMeta.Metas.Fishing.Labels.fishing, 0xFFFFFF, 0x7F7F7F);
    }

    protected Tuple2<Point[], Byte> detectRadioButtons(Rectangle scanRect) {
        final BwMatrixMeta im = BwMatrixMeta.Metas.Globally.Buttons.radioButton;
        im.throwIfNotAvailable();

        final boolean debug = false;

        int positionTolerant = Math.min(10, Configuration.Tolerant.position);
        final byte greenMinDiff = 70;

        try (ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(
                new Offset(Math.max(0, scanRect.x - positionTolerant),
                        Math.max(0, scanRect.y - positionTolerant)),
                scanRect.width + positionTolerant * 2, scanRect.height + positionTolerant * 2)) {
            BufferedImage sc = screenCapturedResult.image;
            saveDebugImage(sc, "detectRadioButtons");

            ArrayList<Point> startingCoords = new ArrayList<>();
            int selectedRadioButtonIndex = -1;
            int skipAfterXIfNotFoundAny = (int) Math.floor((double) sc.getWidth() / 4 * 3);

            for (int y = 0; y < sc.getHeight() - im.getHeight() && startingCoords.size() < 1; y++) {
                for (int x = 0; x < sc.getWidth() - im.getWidth(); x++) {
                    if (x >= skipAfterXIfNotFoundAny && startingCoords.size() == 0)
                        break;

                    final int blackPixelRgb = im.getBlackPixelRgb();
                    ImageUtil.DynamicRgb blackPixelDRgb = im.getBlackPixelDRgb();

                    boolean allGood = true;

                    for (int[] px : im.getBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (!ImageUtil.areColorsSimilar(//
                                blackPixelDRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color, im.getOriginalPixelPart(px[0], px[1]))) {
                            allGood = false;
                            optionalDebug(debug, "detectRadioButtons first match failed at %d,%d (%d,%d), %s vs %s",
                                    x + px[0], y + px[1], px[0], px[1], String.format("%06X", blackPixelRgb),
                                    String.format("%06X", srcRgb));
                            break;
                        }
                    }

                    if (!allGood)
                        continue;

                    for (int[] px : im.getNonBlackPixels()) {
                        int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
                        if (ImageUtil.areColorsSimilar(//
                                blackPixelRgb, //
                                srcRgb, //
                                Configuration.Tolerant.color)) {
                            allGood = false;
                            optionalDebug(debug, "detectRadioButtons second match failed at %d,%d (%d,%d)", x + px[0],
                                    y + px[1], px[0], px[1]);
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

                        int curRadioButtonIndex = startingCoords.size();
                        if (selectedRadioButtonIndex < 0)
                            selectedRadioButtonIndex = curRadioButtonIndex;
                        else {
                            if (selectedRadioButtonIndex != curRadioButtonIndex)
                                throw new InvalidDataException(
                                        "Found more than one selected radio button which is absolutely wrong!");
                        }
                        break;
                    }

                    optionalDebug(debug, "detectRadioButtons captured at %3d,%3d with size %3dx%3d, match at %3d,%3d",
                            screenCapturedResult.x, screenCapturedResult.y, screenCapturedResult.w,
                            screenCapturedResult.h, x, y);
                    startingCoords.add(new Point(x, y));
                }
            }

            if (selectedRadioButtonIndex < 0)
                throw new InvalidDataException("Unable to detect index of selected radio button among %d results",
                        startingCoords.size());

            return new Tuple2<>(
                    startingCoords.stream()
                    .map(c -> new Point(screenCapturedResult.x + c.x, screenCapturedResult.y + c.y)).toArray(Point[]::new),
                    (byte) selectedRadioButtonIndex
            );
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
                    if (continousRed >= 2 && continousRed < maxContinousRed) {
                        info("Detected Auto is not turned on, gonna reactive it after %d seconds", (maxContinousRed - continousRed) * sleepMs / 1_000);
                    }
                    if (continousRed >= maxContinousRed) {
                        info("Detected Auto is not turned on, gonna reactive it soon");
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

    protected boolean tryEnterExpedition(boolean doExpedition, ExpeditionPlace place) {
        if (doExpedition && clickImage(BwMatrixMeta.Metas.Expedition.Labels.idolDimension)) {
            Point p;
            switch (place) {
                case BlubLix:
                    p = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionBlubLix().toScreenCoordinate();
                    break;
                case Mowhi:
                    p = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionMowhi().toScreenCoordinate();
                    break;
                case WizBot:
                    p = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionWizBot().toScreenCoordinate();
                    break;
                case Astamus:
                    p = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionAstamus().toScreenCoordinate();
                    break;
                default:
                    return false;
            }
            mouseMoveAndClickAndHide(p);
            sleep(5_000);
            hideCursor();
            return true;
        }
        return false;
    }

    protected boolean tryEnterWorldBoss(boolean doWorldBoss, UserConfig userConfig,
                                        Supplier<Boolean> isBlocked) {
        Point coord = findImage(BwMatrixMeta.Metas.WorldBoss.Labels.labelInSummonDialog);
        if (coord == null)
            return false;
        if (isBlocked.get() || !doWorldBoss) {
            spamEscape(1);
            return false;
        }
        mouseMoveAndClickAndHide(coord);
        BwMatrixMeta.Metas.WorldBoss.Labels.labelInSummonDialog.setLastMatchPoint(coord.x, coord.y);
        debug("Trying to detect radio buttons");
        Tuple2<Point[], Byte> result = detectRadioButtons(
                Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss());
        Point[] points = result._1;
        int selectedLevel = result._2 + 1;
        info("Found %d world bosses, selected %s", points.length, UserConfig.getWorldBossLevelDesc(selectedLevel));
        if (selectedLevel != userConfig.worldBossLevel) {
            info("Going to changed to %s", UserConfig.getWorldBossLevelDesc(userConfig.worldBossLevel));
            clickRadioButton(userConfig.worldBossLevel, points, "World Boss");
            sleep(3_000);
            result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss());
            selectedLevel = result._2 + 1;
            info("Found %d world bosses, selected %s", result._1.length, UserConfig.getWorldBossLevelDesc(selectedLevel));
            if (selectedLevel != userConfig.worldBossLevel) {
                err("Failure on selecting world boss level");
                spamEscape(1);
                return false;
            }
        }
        sleep(1_000);
        return clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses);
    }

    protected boolean tryEnterRaid(boolean doRaid, UserConfig userConfig, Supplier<Boolean> isBlocked) {
        Point coord = findImage(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog);
        if (coord == null) {
            debug("Label raid not found");
            return false;
        }
        if (isBlocked.get() || !doRaid) {
            spamEscape(1);
            return false;
        }
        mouseMoveAndClickAndHide(coord);
        BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.setLastMatchPoint(coord.x, coord.y);
        Tuple2<Point[], Byte> result = detectRadioButtons(
                Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
        Point[] points = result._1;
        int selectedLevel = result._2 + 1;
        info("Found %d raid levels, selected %s", points.length, UserConfig.getRaidLevelDesc(selectedLevel));
        if (selectedLevel != userConfig.raidLevel) {
            info("Going to changed to %s", UserConfig.getRaidLevelDesc(userConfig.raidLevel));
            clickRadioButton(userConfig.raidLevel, points, "Raid");
            sleep(3_000);
            result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
            selectedLevel = result._2 + 1;
            info("Found %d raid levels, selected %s", result._1.length, UserConfig.getRaidLevelDesc(selectedLevel));
            if (selectedLevel != userConfig.raidLevel) {
                err("Failure on selecting raid level");
                spamEscape(1);
                return false;
            }
        }
        sleep(1_000);
        mouseMoveAndClickAndHide(
                fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog, coord,
                        Configuration.screenResolutionProfile.getOffsetButtonSummonOfRaid()));
        sleep(5_000);
        if (UserConfig.isNormalMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterNormalRaid()));
        } else if (UserConfig.isHardMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterHardRaid()));
        } else if (UserConfig.isHeroicMode(userConfig.raidMode)) {
            mouseMoveAndClickAndHide(
                    fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
                            coord, Configuration.screenResolutionProfile.getOffsetButtonEnterHeroicRaid()));
        } else {
            throw new InvalidDataException("Unknown raid mode value: %d", userConfig.raidMode);
        }
        return true;
    }

    private Point fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta sampleImg, Point sampleImgCoord,
                                                              Offset targetOffset) {
        int x = sampleImgCoord.x - sampleImg.getCoordinateOffset().X;
        int y = sampleImgCoord.y - sampleImg.getCoordinateOffset().Y;
        return new Point(x + targetOffset.X, y + targetOffset.Y);
    }

    protected ExpeditionPlace selectExpeditionPlace() {
        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder("Select a place to do Expedition:\n");
        sb.append(String.format("  1. %s\n", ExpeditionPlace.BlubLix));
        sb.append(String.format("  2. %s\n", ExpeditionPlace.Mowhi));
        sb.append(String.format("  3. %s\n", ExpeditionPlace.WizBot));
        sb.append(String.format("  4. %s\n", ExpeditionPlace.Astamus));
        ExpeditionPlace place = readInput(sb.toString(), null,
                s -> {
                    try {
                        int num = Integer.parseInt(s.trim());
                        if (num == 1)
                            return new Tuple3<>(true, null, ExpeditionPlace.BlubLix);
                        if (num == 2)
                            return new Tuple3<>(true, null, ExpeditionPlace.Mowhi);
                        if (num == 3)
                            return new Tuple3<>(true, null, ExpeditionPlace.WizBot);
                        if (num == 4)
                            return new Tuple3<>(true, null, ExpeditionPlace.Astamus);
                        return new Tuple3<>(false, "Not a valid option", ExpeditionPlace.Astamus);
                    } catch (NumberFormatException ex) {
                        return new Tuple3<>(false, "Not a number", ExpeditionPlace.Astamus);
                    }
                });
        info("Going to farm %s in Expedition", place.toString().toUpperCase());
        return place;
    }

    protected void spamEscape(int expectedCount) {
        int cnt = expectedCount + 4;
        while (cnt-- > 0) {
            sleep(1_000);
            InteractionUtil.Keyboard.sendEscape();
        }
    }

    protected void printRequiresSetting() {
        err("You have to do setting before using this function");
        err("Please launch script '%s' and follow instruction", scriptFileName("setting"));
    }

    private HWND gameWindowHwndByJna = null;

    protected void doCheckGameScreenOffset(AtomicBoolean masterSwicth) {
        debug("doCheckGameScreenOffset");
        try {
            if (Configuration.Features.disableDoCheckGameScreenOffset) {
                info("Feature doCheckGameScreenOffset has been disabled by configuration");
                return;
            }
            if (OS.isLinux || OS.isMac) {
                Process which = Runtime.getRuntime().exec("which xwininfo xdotool ps");
                int exitCode = which.waitFor();
                if (exitCode != 0) {
                    warn("Unable to adjust game screen offset automatically. Require the following tools to be installed: xwininfo, xdotool, ps");
                    return;
                }
            }

            int x = Configuration.gameScreenOffset.X.get();
            int y = Configuration.gameScreenOffset.Y.get();
            IJna jna = getJnaInstance();
            ScreenResolutionProfile srp = Configuration.screenResolutionProfile;
            debug("Active doCheckGameScreenOffset");
            while (!masterSwicth.get()) {
                debug("on loop of doCheckGameScreenOffset");
                try {
                    if (!(jna instanceof AbstractLinuxJna)) {
                        if (gameWindowHwndByJna == null) {
                            gameWindowHwndByJna = jna.getGameWindow();
                            if (gameWindowHwndByJna == null) {
                                debug("Game window could not be found");
                                continue;
                            }
                        }
                    }
                    Tuple4<Boolean, String, Rectangle, Offset> result = jna.locateGameScreenOffset(gameWindowHwndByJna,
                            srp);
                    if (!result._1) {
                        warn("Unable to perform auto adjust game screen offset due to error: %s", result._2);
                        continue;
                    }
                    if (result._4.X != x || result._4.Y != y) {
                        Configuration.gameScreenOffset.set(result._4);
                        x = result._4.X;
                        y = result._4.Y;
                        info(ColorizeUtil.formatInfo, "Game's screen offset has been adjusted automatically to %d,%d", x, y);
                    } else {
                        debug("screen offset not change");
                    }
                } catch (Exception ex2) {
                    warn("Unable to perform auto adjust game screen offset due to error: %s", ex2.getMessage());
                } finally {
                    sleep(60_000);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            warn("Error occurs during doCheckGameScreenOffset");
        }
    }

    protected IJna getJnaInstance() {
        if (Configuration.isSteamProfile)
            return new SteamWindowsJna();
        if (OS.isWin)
            return new MiniClientWindowsJna();
        if (OS.isLinux)
            return new MiniClientLinuxJna();
        if (OS.isMac)
            return new MiniClientMacOsJna();
        throw new NotSupportedException(OS.name);
    }

    protected void adjustScreenOffset() {
        int x = Configuration.gameScreenOffset.X.get();
        int y = Configuration.gameScreenOffset.Y.get();
        try {
            IJna jna = getJnaInstance();
            ScreenResolutionProfile srp = Configuration.screenResolutionProfile;
            Tuple4<Boolean, String, Rectangle, Offset> result = jna.locateGameScreenOffset(null, srp);
            if (result._1) {
                if (result._4.X != x || result._4.Y != y) {
                    Configuration.gameScreenOffset.set(result._4);
                    x = result._4.X;
                    y = result._4.Y;

                    info("Game's screen offset has been adjusted automatically to %d,%d", x, y);
                } else {
                    debug("screen offset not change");
                }
            } else {
                err("Failure detecting screen offset: %s", result._2);
            }
        } catch (Exception e) {
            err("Err detecting screen offset: %s", e.getMessage());
        }
    }

    protected int readProfileNumber(String ask) {
        return readInput(ask, String.format("select a number, min 1, max %d", GenMiniClient.supportMaximumNumberOfAccounts), s -> {
            try {
                int num = Integer.parseInt(s.trim());
                if (num >= 1 && num <= GenMiniClient.supportMaximumNumberOfAccounts)
                    return new Tuple3<>(true, null, num);
                return new Tuple3<>(false, "Value must be in range from 1 to " + GenMiniClient.supportMaximumNumberOfAccounts, 0);
            } catch (NumberFormatException ex) {
                return new Tuple3<>(false, "Not a number", 0);
            }
        });
    }

    protected int readInputLoopCount(String ask) {
        return readInput(ask, "Numeric only", s -> {
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
}
