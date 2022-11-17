package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.Log;
import bh.bot.common.OS;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.extensions.Rad;
import bh.bot.common.jna.*;
import bh.bot.common.types.*;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.RequireSingleInstance;
import bh.bot.common.types.flags.*;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.*;
import bh.bot.common.utils.ColorizeUtil.Cu;
import bh.bot.common.utils.InteractionUtil.Keyboard;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import org.fusesource.jansi.Ansi;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.*;
import static bh.bot.common.utils.Extensions.scriptFileName;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.InteractionUtil.Keyboard.sendEscape;
import static bh.bot.common.utils.InteractionUtil.Keyboard.sendSpaceKey;
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
		
		if (isRequiredToLoadImages())
			BwMatrixMeta.load();
		
		Telegram.setAppName(getAppName());
		
		warn(getLimitationExplain());

		if (launchInfo.shutdownAfterExit) {
			String command;
			if (OS.isWin) {
				command = "shutdown -s -t 0";
			} else if (OS.isLinux) {
				command = "sudo shutdown now";
				try {
					if (0 != Runtime.getRuntime().exec("sudo -nv").waitFor()) {
						err("You must run this bot as sudo to be able to run command '%s' upon completion", command);
						Main.exit(Main.EXIT_CODE_REQUIRE_SUDO);
					}
				} catch (Exception ex) {
					err(ex.getMessage());
					err("Unable to check compatible between `--shutdown` flag and your system, not sure if it works");
				}
			} else {
				throw new NotSupportedException(String.format("Shutdown command is not supported on %s OS", OS.name));
			}

			wrappedInternalRun1(launchInfo);

			final int shutdownAfterMinutes = FlagShutdownAfterExit.shutdownAfterXMinutes;
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
			wrappedInternalRun1(launchInfo);
		}
	}

	private void wrappedInternalRun1(ParseArgumentsResult launchInfo) {
		if (this.getClass().getAnnotation(RequireSingleInstance.class) != null) {
			if (launchInfo.disableMutex) {
				warn("Disabled checking multiple bot instances");
				wrappedInternalRun2(launchInfo);
			} else {
				debug("This application does not allow running multiple instances");
				if (OS.isWin) {
					wrappedInternalRun3ForWindows(launchInfo);
				} else {
					debug("Single instance application currently supports Windows only");
					wrappedInternalRun2(launchInfo);
				}
			}
		} else {
			wrappedInternalRun2(launchInfo);
		}
		tryToCloseGameWindow(launchInfo.closeGameWindowAfterExit);
	}

	private void wrappedInternalRun2(ParseArgumentsResult launchInfo) {
		launchThreadCheckVersion(launchInfo.applicationClass.getAnnotation(AppMeta.class).code());
		showWarningWindowMustClearlyVisible();
		internalRun(launchInfo.arguments);
	}

	private void wrappedInternalRun3ForWindows(ParseArgumentsResult launchInfo) {
		WinNT.HANDLE mutexHandle = null;
		try {
			mutexHandle = Kernel32.INSTANCE.CreateMutex(null, true, String.format("%s-MUTEX", Main.botName));
			if (mutexHandle != null && Kernel32.INSTANCE.GetLastError() != 183) {
				// mutex acquired
				debug("Acquired Mutex handle");
			} else {
				err("'%s' of %s is not allowed to run multiple instances at the same time because it may causes unexpected issues, please close previous process first!!!", this.getClass().getAnnotation(AppMeta.class).name(), Main.botName);
				err("If this message is wrong and you are only running a single instance of %s, please relaunch with flag `%s`", Main.botName, new FlagDisableMutex().getCode());
				Main.exit(Main.EXIT_CODE_MULTIPLE_INSTANCE_DETECTED);
			}
		} catch (Throwable t) {
			dev(t);
			dev("Unable to create mutex");
		}

		try {
			wrappedInternalRun2(launchInfo);
		} finally {
			if (mutexHandle != null) {
				try {
					debug("Releasing mutex handle");
					Kernel32.INSTANCE.ReleaseMutex(mutexHandle);
				} catch (Throwable t) {
					dev(t);
					dev("Problem why trying to release mutex handle");
				}
			}
		}
	}

	protected boolean skipCheckVersion() {
		return false;
	}

	private void launchThreadCheckVersion(final String appCode) {
		CompletableFuture.runAsync(() -> {
			VersionUtil.fetchDisabledFunctions();
			VersionUtil.quitIfCurrentVersionIsRejected(appCode);
		});

		if (skipCheckVersion())
			return;

		//noinspection CodeBlock2Expr
		Rad.exec(33, () -> {
			CompletableFuture.runAsync(() -> {
				if (!VersionUtil.checkForLatestVersion())
					warn("Failure on checking for latest version of %s", Main.botName);
			});
		});
	}

	private void showWarningWindowMustClearlyVisible() {
		Rad.exec(66, () -> {
			if (Configuration.isWebProfile)
				warn("Do NOT zoom in/out the web page while you playing Bit Heroes on web and keep the original resolution is 800x520. If you do zoom the web page, bot won't work");
			Main.showWarningWindowMustClearlyVisible();
		});
	}

	private void tryToCloseGameWindow(boolean closeGameWindowAfterExit) {
		if (!closeGameWindowAfterExit)
			return;

		try {
			int waitXMinutes = FlagCloseGameWindowAfterExit.waitXMinutes;
			long deadline = addSec(waitXMinutes * 60);

			long timeLeft;
			while ((timeLeft = deadline - System.currentTimeMillis()) > 0) {
				info(ColorizeUtil.formatError,  "Game window is going to be closed after %d minute%s", waitXMinutes, waitXMinutes > 1 ? "s" : "");
				waitXMinutes--;
				sleep((int) Math.min(60_000, timeLeft + 1));
			}
		} catch (Exception ignored2) {
			//
		} finally {
			try {
				getJnaInstance().tryToCloseGameWindow();
			} catch (Exception ignored) {
				//
			}
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
			// noinspection ResultOfMethodCallIgnored
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
		File file = Paths
				.get("out", "images", getAppCode(), prefix + "_" + System.currentTimeMillis() + ".bmp")
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

	public String getArgHint() {
		return null;
	}

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
				.filter(x -> !x.isGlobalFlag() && x.isSupportedByApp(this) && !x.hide())
				.collect(Collectors.toList());
		if (localFlags.size() > 0) {
			sb.append("\nFlags:");
			for (FlagPattern localFlag : localFlags)
				sb.append(localFlag);
		}
		// Global flags:
		List<FlagPattern> globalFlags = flagPatterns.stream() //
				.filter(FlagPattern::isGlobalFlag) //
				.filter(x -> x.isSupportedByApp(this) && !x.hide()) //
				.collect(Collectors.toList());
		sb.append("\nGlobal flags:");
		for (FlagPattern globalFlag : globalFlags.stream() //
				.filter(x -> !(x instanceof FlagResolution)) //
				.collect(Collectors.toList()))
			sb.append(globalFlag);
		List<FlagPattern> flagsResolution = globalFlags.stream() //
				.filter(x -> x instanceof FlagResolution) //
				.collect(Collectors.toList());
		if (flagsResolution.size() > 0) {
			for (FlagPattern globalFlag : flagsResolution)
				sb.append(globalFlag);
		}
		int defaultMainLoopInterval = getDefaultMainLoopInterval();
		if (defaultMainLoopInterval > 0) {
			sb.append("\nDefault main loop interval is ");
			if (defaultMainLoopInterval < 1_000) {
				sb.append(defaultMainLoopInterval);
				sb.append(" milliseconds");
			} else if (defaultMainLoopInterval % 1_000 == 0) {
				sb.append(defaultMainLoopInterval / 1_000);
				sb.append(" seconds (");
				sb.append(defaultMainLoopInterval);
				sb.append(" milliseconds)");
			} else {
				sb.append("around ");
				sb.append(Math.round((double)defaultMainLoopInterval / 1_000));
				sb.append(" seconds (");
				sb.append(defaultMainLoopInterval);
				sb.append(" milliseconds)");
			}

			FlagAlterLoopInterval flagAlterLoopInterval = new FlagAlterLoopInterval();
			if (flagAlterLoopInterval.isSupportedByApp(this)) {
				sb.append(" and able to be altered using flag ");
				sb.append(flagAlterLoopInterval.getCode());
			} else {
				sb.append(" <this function does not support flag ");
				sb.append(flagAlterLoopInterval.getCode());
				sb.append(">");
			}
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
						Configuration.Tolerant.color, //
						im.getOriginalPixelPart(px[0], px[1])
				)) {
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
				new Offset(Math.max(0, scanRect.x - positionTolerant), Math.max(0, scanRect.y - positionTolerant)),
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
										"Found more than one selected radio button which is absolutely wrong!"
								);
						}
						break;
					}

					optionalDebug(debug, "detectRadioButtons captured at %3d,%3d with size %3dx%3d, match at %3d,%3d",
							screenCapturedResult.x, screenCapturedResult.y,
							screenCapturedResult.w, screenCapturedResult.h,
							x, y
					);
					startingCoords.add(new Point(x, y));
				}
			}

			if (selectedRadioButtonIndex < 0)
				throw new InvalidDataException("Unable to detect index of selected radio button among %d results",
						startingCoords.size());

			return new Tuple2<>(
					startingCoords.stream()
					.map(c -> new Point(screenCapturedResult.x + c.x, screenCapturedResult.y + c.y))
					.toArray(Point[]::new),
					(byte) selectedRadioButtonIndex
			);
		}
	}

	private static final int smallTalkSleepSecs = 60;
	private static final int smallTalkSleepSecsWhenClicked = 3;
	private static final int detectDcSleepSecs = 60;
	private static final int reactiveAutoSleepSecs = 10;
	private static final int closeEnterGameDialogNewsSleepSecs = 60;
	private static final int persuadeSleepSecs = 60;
	private static final int persuadeSleepSecsIntervalInCaseManual = 30;
	private static final int persuadeSleepSecsAwaitAction = 20;
	private static final int showWarningWorldBossTeamSleepSecs = 60;

	protected void internalDoSmallTasks(AtomicBoolean masterSwitch, SmallTasks st) {
		try {
			long nextSmallTalkEpoch = addSec(smallTalkSleepSecs);
			long nextDetectDcEpoch = addSec(detectDcSleepSecs);
			long nextReactiveAuto = addSec(reactiveAutoSleepSecs);
			final AtomicInteger continousRed = new AtomicInteger(0);
			long nextCloseEnterGameDialogNews = addSec(closeEnterGameDialogNewsSleepSecs);
			final AtomicInteger continousPersuadeScreen = new AtomicInteger(0);
			long nextPersuade = addSec(persuadeSleepSecs);
			boolean persuade = st.persuade && !argumentInfo.disablePersuade;
			long nextShowWarningWorldBossTeam = addSec(showWarningWorldBossTeamSleepSecs);
			boolean showWarningWorldBossTeam = st.showWarningWorldBossTeam;

			if (persuade) {
				info("Auto persuade had been turned on, that means in case of any persuade screen appears, bot will automatically persuade the monster with Gold. If you want to disable this feature, you can use '%s' flag", FlagDisablePersuade.FlagName);
				if (Configuration.enableDevFeatures) {
					final String fileBribeName = "bribe.txt";
					File fBribe = new File(fileBribeName);
					if (fBribe.exists() && fBribe.isFile()) {
						List<String> familiarsToBribe = Files.readAllLines(fBribe.toPath()).stream().filter(StringUtil::isNotBlank).map(String::trim).collect(Collectors.toList());
						if (familiarsToBribe.size() > 0) {
							FlagBribe flagBribe = new FlagBribe();
							ArrayList<Familiar> familiars = new ArrayList<>();
							boolean ignoreFile = false;
							for (String famName : familiarsToBribe)
								try {
									familiars.add(flagBribe.parseParam(String.format("%s=%s", flagBribe.getCode(), famName)));
								} catch (NotSupportedException ignored) {
									throw new InvalidDataException("Familiar's name '%s' within file %s is not supported!", famName, fileBribeName);
								} catch (Exception ex2) {
									dev(ex2);
									err("Error occurs while trying to parse %s file. Content within that file will be ignored. Please raise an issue on my GitHub repo", fileBribeName);
									ignoreFile = true;
									break;
								}
							if (!ignoreFile)
								for (Familiar fam : familiars)
									argumentInfo.addFamiliarToBribeWithGems(fam);
						}
					}
				}

				/* // TODO enable this if Bribe feature to be enabled again
					for (Familiar f : argumentInfo.familiarToBribeWithGems)
						warn("Will persuade %s with gems", f.name());
				*/
			}

			if (st.persuade && argumentInfo.disablePersuade)
				info(Cu.i()
						.yellow("** WARNING ** ")
						.red("Auto persuade/bribe has been disabled")
						.yellow(" so please ")
						.red("turn on in-game's auto persuade")
						.yellow(" for all kind of familiars or ")
						.red("always stay in front of your computer")
						.yellow(" to persuade/bribe manually, otherwise bot can't continue your tasks and becomes waste of resources")
						.reset()
				);

			while (!masterSwitch.get()) {
				sleep(1_000);

				if (st.clickTalk && nextSmallTalkEpoch <= System.currentTimeMillis())
					nextSmallTalkEpoch = doClickTalk();

				if (st.clickDisconnect && nextDetectDcEpoch <= System.currentTimeMillis())
					nextDetectDcEpoch = detectDisconnected(masterSwitch);

				if (st.reactiveAuto && nextReactiveAuto <= System.currentTimeMillis())
					nextReactiveAuto = autoReactiveAuto(continousRed);

				if (st.autoExit)
					autoExit(masterSwitch);

				if (st.closeEnterGameNewsDialog && nextCloseEnterGameDialogNews <= System.currentTimeMillis())
					nextCloseEnterGameDialogNews = closeEnterGameDialogNews();

				if (persuade && nextPersuade <= System.currentTimeMillis())
					nextPersuade = doPersuade(continousPersuadeScreen);

				if (showWarningWorldBossTeam && nextShowWarningWorldBossTeam <= System.currentTimeMillis())
				{
					warningWatchWorldBossTeam(ColorizeUtil.formatWarning);
					nextShowWarningWorldBossTeam = addSec(showWarningWorldBossTeamSleepSecs);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Telegram.sendMessage("Error occurs during execution: " + ex.getMessage(), true);
			masterSwitch.set(true);
		}
	}

	private long addSec(int secs) {
		return System.currentTimeMillis() + secs * 1_000L;
	}

	protected static class SmallTasks {
		public final boolean clickTalk;
		public final boolean clickDisconnect;
		public final boolean reactiveAuto;
		public final boolean autoExit;
		public final boolean closeEnterGameNewsDialog;
		public final boolean persuade;
		public final boolean showWarningWorldBossTeam;

		private SmallTasks(Builder b) {
			this.clickTalk = b.f(0);
			this.clickDisconnect = b.f(1);
			this.reactiveAuto = b.f(2);
			this.autoExit = b.f(3);
			this.closeEnterGameNewsDialog = b.f(4);
			this.persuade = b.f(5);
			this.showWarningWorldBossTeam = b.f(6);
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			private final boolean[] flags = new boolean[10];

			private Builder set(int index) {
				this.flags[index] = true;
				return this;
			}

			private boolean f(int index) {
				return this.flags[index];
			}

			public SmallTasks build() {
				return new SmallTasks(this);
			}

			public Builder clickTalk() {
				return this.set(0);
			}

			public Builder clickDisconnect() {
				return this.set(1);
			}

			public Builder reactiveAuto() {
				return this.set(2);
			}

			public Builder autoExit() {
				return this.set(3);
			}

			public Builder closeEnterGameNewsDialog() {
				return this.set(4);
			}

			public Builder persuade() {
				return this.set(5);
			}

			public Builder warningWorldBossTeam() {
				return this.set(6);
			}
		}
	}

	private List<Tuple2<BwMatrixMeta, Familiar>> persuadeTargets = null;
	private boolean anyManuallyPersuade = false;
	private long doPersuade(AtomicInteger continousPersuadeScreen) {
		Point pBribeButton = findImage(BwMatrixMeta.Metas.Globally.Buttons.persuadeBribe);
		Point pPersuadeButton = pBribeButton != null ? null : findImage(BwMatrixMeta.Metas.Globally.Buttons.persuade);
		if (pPersuadeButton != null || pBribeButton != null) {
			info("Found Persuade interface");
			int continous = continousPersuadeScreen.addAndGet(1);
			if (continous > 0) {
				if (continous % 10 == 1) {
					Telegram.sendMessage("Found persuade screen", true);
					saveCurrentScreenShot(true);
				}

				if (persuadeTargets == null)
					persuadeTargets = Arrays.asList(
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.ragnar, Familiar.Ragnar),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.kaleido, Familiar.Kaleido),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.violace, Familiar.Violace),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.oevor, Familiar.Oevor),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.grimz, Familiar.Grimz),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.quirrel, Familiar.Quirrel),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.gobby, Familiar.Gobby),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.rugumz, Familiar.Rugumz),
							new Tuple2<>(BwMatrixMeta.Metas.Persuade.Labels.moghur, Familiar.Moghur)
					);

				boolean doPersuadeGold = true;

				/* // TODO enable this if Bribe feature to be enabled again
					if (argumentInfo.familiarToBribeWithGems.size() > 0) {
						for (Tuple2<BwMatrixMeta, Familiar> target : persuadeTargets) {
							PersuadeState ps = persuade(target._1, target._2, pPersuadeButton, pBribeButton);
							if (ps == PersuadeState.NotAvailable) {
								doPersuadeGold = false;
								break;
							}
							if (ps == PersuadeState.SuccessGem || ps == PersuadeState.SuccessGold) {
								doPersuadeGold = false;
								break;
							}
						}
					}
				*/

				if (doPersuadeGold) {
					dev("doPersuadeGold");
					persuade(true, pPersuadeButton, pBribeButton);
				}

				return addSec(persuadeSleepSecsAwaitAction);
			} else {
				info("Found persuade screen");
			}
		} else {
			continousPersuadeScreen.set(0);
		}
		return addSec(anyManuallyPersuade ? persuadeSleepSecsIntervalInCaseManual : persuadeSleepSecs);
	}

	@SuppressWarnings("SameParameterValue")
	private void saveCurrentScreenShot(boolean ignoreError) {
		try {
			int x = Configuration.gameScreenOffset.X.get();
			int y = Configuration.gameScreenOffset.Y.get();
			int w = Configuration.screenResolutionProfile.getSupportedGameResolutionWidth();
			int h = Configuration.screenResolutionProfile.getSupportedGameResolutionHeight();

			BufferedImage sc = InteractionUtil.Screen.captureScreen(x, y, w, h);
			try {
				saveImage(sc, "persuade");
			} catch (Exception ex2) {
				ImageUtil.freeMem(sc);
				throw ex2;
			}
		} catch (Exception ex) {
			if (ignoreError)
				dev("Ignored error upon saving current screenshot during persuade: %s", ex.getMessage());
			else
				throw ex;
		}
	}

	private PersuadeState persuade(BwMatrixMeta im, Familiar familiar, Point pPersuadeButton, Point pBribeButton) {
		String name = familiar.name().toUpperCase();

		if (im.notAvailable) {
			warn("Persuading %s has not yet been implemented for this resolution profile", name);
			return PersuadeState.NotAvailable;
		}

		Point pFamiliar = findImage(im);
		if (pFamiliar == null)
			return PersuadeState.NotTargetFamiliar;

		info("This is '%s' familiar", name);

		if (!argumentInfo.familiarToBribeWithGems.contains(familiar)) {
			info("%s is NOT in the list to bribe with gems so going to persuade it with gold", name);
			persuade(true, pPersuadeButton, pBribeButton);
			return PersuadeState.SuccessGold;
		}

		try {
			info("Going to persuade %s with gems", name);
			persuade(false, pPersuadeButton, pBribeButton);
			warn(name);
			Telegram.sendMessage(name, false);
		} catch (Exception e) {
			e.printStackTrace();
			err(name);
			Telegram.sendMessage(String.format("%s failure: %s", name, e.getMessage()), true);
		}
		return PersuadeState.SuccessGem;
	}

	private enum PersuadeState {
		NotAvailable, SuccessGold, SuccessGem, NotTargetFamiliar
	}

	private void persuade(boolean gold, Point pPersuadeButton, Point pBribeButton) {
		if (pPersuadeButton != null)
			dev("Persuade: %-3d.%3d", pPersuadeButton.x, pPersuadeButton.y);
		if (pBribeButton != null)
			dev("Bribe: %-3d.%3d", pBribeButton.x, pBribeButton.y);
		Point p = null;
		if (gold) {
			if (pPersuadeButton != null) {
				p = pPersuadeButton;
			} else if (pBribeButton != null) {
				p = fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Globally.Buttons.persuadeBribe, pBribeButton, Configuration.screenResolutionProfile.getOffsetButtonPersuade());
			}
		} else {
			if (pBribeButton != null) {
				p = pBribeButton;
			} else if (pPersuadeButton != null) {
				p = fromRelativeToAbsoluteBasedOnPreviousResult(BwMatrixMeta.Metas.Globally.Buttons.persuade, pPersuadeButton, Configuration.screenResolutionProfile.getOffsetButtonBribePersuade());
			}
		}

		if (p == null)
			throw new InvalidDataException("Implemented wrongly");

		Log.printStackTrace();
		dev("persuade: gold=%b, p=%d.%d", gold, p.x, p.y);
		mouseMoveAndClickAndHide(p);
		sleep(5_000);
		Keyboard.sendEnter();
		anyManuallyPersuade = true;
	}

	private long doClickTalk() {
		if (clickImage(BwMatrixMeta.Metas.Globally.Buttons.talkRightArrow)) {
			debug("clicked talk");
			return addSec(smallTalkSleepSecsWhenClicked);
		} else {
			debug("No talk");
			return addSec(smallTalkSleepSecs);
		}
	}

	private long detectDisconnected(AtomicBoolean masterSwitch) {
		if (clickImage(BwMatrixMeta.Metas.Globally.Buttons.reconnect)) {
			try {
				Telegram.sendMessage("Disconnected", true);
			} finally {
				masterSwitch.set(true);
			}
		}
		return addSec(detectDcSleepSecs);
	}

	@SuppressWarnings("FieldCanBeLocal")
	private final byte maxContinousRed = 6;

	private long autoReactiveAuto(AtomicInteger continousRed) {
		long next = addSec(reactiveAutoSleepSecs);

		Point point = findImage(BwMatrixMeta.Metas.Globally.Buttons.autoG);
		if (point == null) {
			debug("AutoG button not found");
			point = findImage(BwMatrixMeta.Metas.Globally.Buttons.autoR);
			if (point == null) {
				debug("AutoR button not found");
				return next;
			}
		}

		debug("Found the Auto button at %d,%d", point.x, point.y);

		Color color = getPixelColor(point.x - 5, point.y);

		if (ImageUtil.isGreenLikeColor(color)) {
			debug("Auto is currently ON (green)");
			continousRed.set(0);
			return next;
		}

		if (!ImageUtil.isRedLikeColor(color)) {
			debug("Red Auto not found");
			return next;
		}

		int cr = continousRed.addAndGet(1);

		if (cr < maxContinousRed) {
			if (cr >= 2)
				info( //
						"Detected Auto is not turned on, gonna reactive it after %d seconds", //
						(maxContinousRed - cr) * reactiveAutoSleepSecs //
				);
			return next;
		}

		info("Detected Auto is not turned on, gonna reactive it soon");
		moveCursor(point);
		sleep(100);
		if (cr % 4 == 0)
			sendSpaceKey();
		else
			mouseClick();
		hideCursor();

		info("Sent re-active");
		sleep(1_000);

		return next + 1_000;
	}

	private long closeEnterGameDialogNews() {
		if (clickImage(BwMatrixMeta.Metas.Globally.Dialogs.news))
			sendEscape();
		return addSec(closeEnterGameDialogNewsSleepSecs);
	}

	protected final long applicationStartTime = System.currentTimeMillis();

	private void autoExit(AtomicBoolean masterSwitch) {
		if (argumentInfo.exitAfterXSecs < 1)
			return;

		long applicationExpectedExitTime = applicationStartTime + argumentInfo.exitAfterXSecs * 1_000L;
		long now = System.currentTimeMillis();
		if (applicationExpectedExitTime <= now) {
			masterSwitch.set(true);
			info("Application is going to exit now");
			return;
		}

		long exitAfterXSecs = (applicationExpectedExitTime - System.currentTimeMillis()) / 1_000;

		int divBy;
		if (exitAfterXSecs >= 3600)
			divBy = 900;
		else if (exitAfterXSecs >= 600)
			divBy = 600;
		else if (exitAfterXSecs >= 300)
			divBy = 120;
		else if (exitAfterXSecs >= 60)
			divBy = 60;
		else if (exitAfterXSecs >= 10)
			divBy = 10;
		else
			divBy = 1;

		if (exitAfterXSecs % divBy == 0)
			info("Exit after %s", TimeUtil.niceTimeLong(exitAfterXSecs));
	}

	protected boolean tryEnterExpedition(boolean doExpedition, byte place) {
		if (!doExpedition)
			return false;

		Offset o = null;
		if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.hallowedDimension)) {
			if (place == 1)
				o = Configuration.screenResolutionProfile.getOffsetEnterHallowedDimensionGooGarum();
			else if (place == 2)
				o = Configuration.screenResolutionProfile.getOffsetEnterHallowedDimensionSvord();
			else if (place == 3)
				o = Configuration.screenResolutionProfile.getOffsetEnterHallowedDimensionTwimbo();
			else if (place == 4)
				o = Configuration.screenResolutionProfile.getOffsetEnterHallowedDimensionX5T34M();
		} else if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.idolDimension)) {
			if (place == 1)
				o = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionBlubLix();
			else if (place == 2)
				o = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionMowhi();
			else if (place == 3)
				o = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionWizBot();
			else if (place == 4)
				o = Configuration.screenResolutionProfile.getOffsetEnterIdolDimensionAstamus();
		} else if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.infernoDimension)) {
			if (place == 1)
				o = Configuration.screenResolutionProfile.getOffsetEnterInfernoDimensionRaleib();
			else if (place == 2)
				o = Configuration.screenResolutionProfile.getOffsetEnterInfernoDimensionBlemo();
			else if (place == 3)
				o = Configuration.screenResolutionProfile.getOffsetEnterInfernoDimensionGummy();
			else if (place == 4)
				o = Configuration.screenResolutionProfile.getOffsetEnterInfernoDimensionZarlock();
		} else if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.battleBards)) {
			if (place == 1)
				o = Configuration.screenResolutionProfile.getOffsetEnterBattleBardsHero();
			else if (place == 2)
				o = Configuration.screenResolutionProfile.getOffsetEnterBattleBardsBurning();
			else if (place == 3)
				o = Configuration.screenResolutionProfile.getOffsetEnterBattleBardsMelvapaloozo();
			else if (place == 4)
				o = Configuration.screenResolutionProfile.getOffsetEnterBattleBardsBitstock();
		} else if (clickImage(BwMatrixMeta.Metas.Expedition.Labels.jammieDimension)) {
			if (place == 1)
				o = Configuration.screenResolutionProfile.getOffsetEnterJammieDimensionZorgo();
			else if (place == 2)
				o = Configuration.screenResolutionProfile.getOffsetEnterJammieDimensionYackerz();
			else if (place == 3)
				o = Configuration.screenResolutionProfile.getOffsetEnterJammieDimensionVionot();
			else if (place == 4)
				o = Configuration.screenResolutionProfile.getOffsetEnterJammieDimensionGrampa();
		}

		if (o != null) {
			Point p = o.toScreenCoordinate();
			mouseMoveAndClickAndHide(p);
			sleep(5_000);
			hideCursor();
			return true;
		}

		return false;
	}

	protected boolean tryEnterWorldBoss(boolean doWorldBoss, UserConfig userConfig, Supplier<Boolean> isBlocked) {
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
			info("Found %d world bosses, selected %s", result._1.length,
					UserConfig.getWorldBossLevelDesc(selectedLevel));
			if (selectedLevel != userConfig.worldBossLevel) {
				err("Failure on selecting world boss level\nThis problem probably caused by the game client itself, please manually select another World Boss (except the first one) and press Summon, need time it should be OK");
				spamEscape(1);
				return false;
			}
		}
		sleep(1_000);
		return clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses);
	}

	protected boolean tryEnterRaid(boolean doRaid, UserConfig userConfig, Supplier<Boolean> isBlocked) {
		Point coord = findImage(BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog);
		if (coord == null)
			return false;

		if (isBlocked.get() || !doRaid) {
			spamEscape(1);
			return false;
		}
		mouseMoveAndClickAndHide(coord);
		BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog.setLastMatchPoint(coord.x, coord.y);
		Tuple2<Point[], Byte> result = detectRadioButtons(
				Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid()
		);
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
				err("Failure on selecting raid level\nThis problem probably caused by the game client itself, please manually select another Raid (not R1-T4) and press Summon, need time it should be OK");
				spamEscape(1);
				return false;
			}
		}
		sleep(1_000);
		mouseMoveAndClickAndHide(
				fromRelativeToAbsoluteBasedOnPreviousResult(
						BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
						coord,
						Configuration.screenResolutionProfile.getOffsetButtonSummonOfRaid()
				)
		);
		sleep(5_000);
		if (UserConfig.isNormalMode(userConfig.raidMode)) {
			mouseMoveAndClickAndHide(
					fromRelativeToAbsoluteBasedOnPreviousResult(
							BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
							coord, 
							Configuration.screenResolutionProfile.getOffsetButtonEnterNormalRaid()
					)
			);
		} else if (UserConfig.isHardMode(userConfig.raidMode)) {
			mouseMoveAndClickAndHide(
					fromRelativeToAbsoluteBasedOnPreviousResult(
							BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
							coord, 
							Configuration.screenResolutionProfile.getOffsetButtonEnterHardRaid()
					)
			);
		} else if (UserConfig.isHeroicMode(userConfig.raidMode)) {
			mouseMoveAndClickAndHide(
					fromRelativeToAbsoluteBasedOnPreviousResult(
							BwMatrixMeta.Metas.Raid.Labels.labelInSummonDialog,
							coord, 
							Configuration.screenResolutionProfile.getOffsetButtonEnterHeroicRaid()
					)
			);
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

	protected byte selectExpeditionPlace() {
		StringBuilder sb = new StringBuilder("Select a place to do Expedition:\n");
		final Tuple2<Byte, Byte> expeditionPlaceRange = UserConfig.getExpeditionPlaceRange();
		for (int i = expeditionPlaceRange._1; i <= expeditionPlaceRange._2; i++)
			sb.append(String.format("  %d. %s\n", i, UserConfig.getExpeditionPlaceDesc(i)));
		byte place = (readInput(sb.toString(), null, s -> {
			try {
				int num = Byte.parseByte(s.trim());
				if (expeditionPlaceRange._1 <= num && num <= expeditionPlaceRange._2)
					return new Tuple3<>(true, null, num);
				return new Tuple3<>(false, "Not a valid option", 0);
			} catch (NumberFormatException ex) {
				return new Tuple3<>(false, "Not a number", 0);
			}
		})).byteValue();
		info("Going to farm %s in Expedition", UserConfig.getExpeditionPlaceDesc(place));
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
						info(ColorizeUtil.formatInfo, "Game's screen offset has been adjusted automatically to %d,%d",
								x, y);
					} else {
						debug("screen offset not change");
					}
					
					if (jna instanceof SteamWindowsJna && gameWindowHwndByJna != null) {
						jna.setGameWindowOnTop(gameWindowHwndByJna);
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
		if (OS.isWin)
			return Configuration.isSteamProfile ? new SteamWindowsJna() : new MiniClientWindowsJna();
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

	protected String readCfgProfileName(String ask, String desc) {
		StringBuilder sb = new StringBuilder();
		boolean hasExistingProfile = false;
		try {
			final String prefix = "readonly.";
			final String suffix = ".user-config.properties";
			for (File file : Arrays.stream(Objects.requireNonNull(new File(".").listFiles())).filter(File::isFile)
					.sorted().collect(Collectors.toList())) {
				String name = file.getName();
				if (!name.startsWith(prefix) || !name.endsWith(suffix))
					continue;
				if (name.length() < prefix.length() + suffix.length() + 1)
					continue;
				StringBuilder sb2 = new StringBuilder();
				ArrayList<Byte> chars = new ArrayList<>();
				for (char c : name.toCharArray())
					chars.add((byte) c);
				chars.stream().skip(prefix.length()).limit(name.length() - prefix.length() - suffix.length())
						.forEach(c -> sb2.append((char) c.byteValue()));
				String cfgProfileName = sb2.toString();
				if (cfgProfileName.length() > 0) {
					if (!ValidationUtil.isValidUserProfileName(cfgProfileName))
						continue;
					if (!hasExistingProfile) {
						hasExistingProfile = true;
						sb.append("Existing profiles:\n");
					}
					sb.append("  ");
					sb.append(cfgProfileName);
					sb.append('\n');
				}
			}
			if (hasExistingProfile)
				sb.append('\n');
		} catch (Exception ex) {
			err("Problem while trying to list existing files in current directory: %s", ex.getMessage());
		}
		sb.append(ask);

		if (!hasExistingProfile)
			sb.append(Cu.i().red("\nYou haven't configurated any profile, please launch ").yellow(Extensions.scriptFileName("setting")).red(" to make one").reset().toString());
		
		return readInput(sb.toString(), desc, s -> {
			s = s.trim().toLowerCase();
			if (!ValidationUtil.isValidUserProfileName(s))
				return new Tuple3<>(false,
						"Not a valid profile name, correct format should be: " + FlagProfileName.formatDesc, null);
			return new Tuple3<>(true, null, s);
		}).trim().toLowerCase();
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

	protected UserConfig getPredefinedUserConfigFromProfileName(String ask) throws IOException {
		String cfgProfileName = this.argumentInfo.cfgProfileName;
		if (StringUtil.isBlank(cfgProfileName))
			cfgProfileName = readCfgProfileName(ask, null);
		Tuple2<Boolean, UserConfig> resultLoadUserConfig = Configuration.loadUserConfig(cfgProfileName);
		if (!resultLoadUserConfig._1) {
			err("Profile name could not be found (check existence of file readonly.<profile_name>.user-config.properties)");
			printRequiresSetting();
			Main.exit(Main.EXIT_CODE_INCORRECT_LEVEL_AND_DIFFICULTY_CONFIGURATION);
		}
		return resultLoadUserConfig._2;
	}

	protected void warningPvpTargetSelectionCase() {
		info(Cu.i().yellow("** WARNING ** ").red("about selecting PVP target").yellow(" feature, to prevent wrong targeting and un-expected loss on other target-selectable ranking like GVG... (which having the same target-selection method), ").cyan("while doing AFK").yellow(", this feature works and ").cyan("only works when bot itself attends to PVP").yellow(" by selecting the PVP icon (top left of game screen). That means if you select the PVP icon yourself or enter PVP before bot click etc.., it only select the first line as target as default").reset());
	}

	protected void warningWatchWorldBossTeam(Function<Ansi, Ansi> ansiFormat) {
		info(ansiFormat, "*** NOTICE: REMEMBER YOU HAVE TO WATCH/CHECK THE GAME SOMETIME TO PREVENT UN-EXPECTED HANG/LOSS DUE TO UN-MANAGED BEHAVIORS LIKE MISSING MEMBERS, RE-GROUP FAILED, INCORRECT GROUP MATCHING...ETC ***");
	}

	protected int getDefaultMainLoopInterval() {
		return 0;
	}
}