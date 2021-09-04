package bh.bot.app;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.optionalDebug;
import static bh.bot.common.Log.printIfIncorrectImgPosition;
import static bh.bot.common.Log.warn;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.InteractionUtil.Mouse.clickRadioButton;
import static bh.bot.common.utils.InteractionUtil.Mouse.hideCursor;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseClick;
import static bh.bot.common.utils.InteractionUtil.Mouse.mouseMoveAndClickAndHide;
import static bh.bot.common.utils.InteractionUtil.Mouse.moveCursor;
import static bh.bot.common.utils.InteractionUtil.Screen.captureElementInEstimatedArea;
import static bh.bot.common.utils.InteractionUtil.Screen.captureScreen;
import static bh.bot.common.utils.InteractionUtil.Screen.getPixelColor;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.ThreadUtil.sleep;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
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

import javax.imageio.ImageIO;

import bh.bot.common.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.Main;
import bh.bot.app.farming.ExpeditionApp.ExpeditionPlace;
import bh.bot.common.Configuration;
import bh.bot.common.Configuration.Offset;
import bh.bot.common.Telegram;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.ParseArgumentsResult;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.flags.FlagPattern;
import bh.bot.common.types.flags.FlagResolution;
import bh.bot.common.types.flags.Flags;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.InteractionUtil.Screen.ScreenCapturedResult;

public abstract class AbstractApplication {
	protected ParseArgumentsResult argumentInfo;
	private final String appCode = this.getClass().getAnnotation(AppCode.class).code();

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
		List<FlagPattern> localFlags = flagPatterns.stream().filter(x -> !x.isGlobalFlag())
				.collect(Collectors.toList());
		if (localFlags.size() > 0) {
			sb.append("\nFlags:");
			for (FlagPattern localFlag : localFlags)
				sb.append(localFlag);
		}
		// Global flags:
		List<FlagPattern> globalFlags = flagPatterns.stream().filter(x -> x.isGlobalFlag())
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

		try (ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(im)){
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

	private final byte greenMinDiff = 70;

	protected Tuple2<Point[], Byte> detectRadioButtons(Rectangle scanRect) {
		final BwMatrixMeta im = BwMatrixMeta.Metas.Globally.Buttons.radioButton;
		im.throwIfNotAvailable();

		final boolean debug = false;

		int positionTolerant = Math.min(10, Configuration.Tolerant.position);

		try (ScreenCapturedResult screenCapturedResult = captureElementInEstimatedArea(
				new Configuration.Offset(Math.max(0, scanRect.x - positionTolerant),
						Math.max(0, scanRect.y - positionTolerant)),
				scanRect.width + positionTolerant * 2, scanRect.height + positionTolerant * 2)){
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
					// im.setLastMatchPoint(startingCoord.x, startingCoord.y);
					startingCoords.add(new Point(x, y));
				}
			}

			if (selectedRadioButtonIndex < 0)
				throw new InvalidDataException("Unable to detect index of selected radio button among %d results",
						startingCoords.size());

			return new Tuple2<>(startingCoords.stream()
					.map(c -> new Point(screenCapturedResult.x + c.x, screenCapturedResult.y + c.y))
					.collect(Collectors.toList()).toArray(new Point[0]), (byte) selectedRadioButtonIndex);
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

	protected <T> T readInput(String ask, String desc,
			Function<String, Tuple3<Boolean, String, T>> transform) {
		return readInput(ask, desc, transform, false);
	}

	protected <T> T readInput(String ask, String desc,
			Function<String, Tuple3<Boolean, String, T>> transform, boolean allowBlankAndIfBlankThenReturnNull) {
		return readInput(ask, desc, null, transform, allowBlankAndIfBlankThenReturnNull);
	}

	protected <T> T readInput(String ask, String desc,
			Supplier<List<String>> selectedOptionsInfoProvider, Function<String, Tuple3<Boolean, String, T>> transform,
			boolean allowBlankAndIfBlankThenReturnNull) {
		try {
			BufferedReader br = Main.getBufferedReader();
			String input;
			while (true) {
				info("\n\n\n\n==========================");
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

	protected boolean tryEnterWorldBoss(boolean doWorldBoss, Configuration.UserConfig userConfig,
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
		info("Found %d, selected %d", points.length, selectedLevel);
		if (selectedLevel != userConfig.worldBossLevel)
			clickRadioButton(userConfig.worldBossLevel, points, "World Boss");
		sleep(3_000);
		result = detectRadioButtons(Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss());
		selectedLevel = result._2 + 1;
		if (selectedLevel != userConfig.worldBossLevel) {
			err("Failure on selecting world boss level");
			spamEscape(1);
			return false;
		}
		sleep(1_000);
		return clickImage(BwMatrixMeta.Metas.WorldBoss.Buttons.summonOnListingWorldBosses);
	}

	protected ExpeditionPlace selectExpeditionPlace() {
		StringBuilder sb = new StringBuilder();
		sb.append("Select a place to do Expedition:\n");
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
		info("You have selected to farm %s on expedition", place.toString().toUpperCase());
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
		err("Please launch script 'setting.%s' and follow instruction", Configuration.OS.isWin ? "bat" : "sh");
	}

	private HWND gameWindowHwndByJna = null;

	protected void doCheckGameScreenOffset(AtomicBoolean masterSwicth) {
		debug("doCheckGameScreenOffset");
		try {
			if (Configuration.Features.disableDoCheckGameScreenOffset) {
				info("Feature doCheckGameScreenOffset has been disabled by configuration");
				return;
			}
			if (!Configuration.OS.isWin) {
				Process which = Runtime.getRuntime().exec("which xwininfo xdotool ps");
				int exitCode = which.waitFor();
				if (exitCode != 0) {
					err("Unable to adjust game screen offset automatically: exit code of which command is %d. Require the following tools to be installed: xwininfo, xdotool, ps",
							exitCode);
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
						err(result._2);
						continue;
					}
					if (result._4.X != x || result._4.Y != y) {
						Configuration.gameScreenOffset.set(result._4);
						x = result._4.X;
						y = result._4.Y;
						info("Game's screen offset has been adjusted automatically to %d,%d", x, y);
					} else {
						debug("screen offset not change");
					}
				} catch (Exception e2) {
					err(e2.getMessage());
				} finally {
					sleep(60_000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			err("Error occured during doCheckGameScreenOffset");
		}
	}

	protected IJna getJnaInstance() {
		if (Configuration.isSteamProfile)
			return new SteamWindowsJna();
		if (Configuration.OS.isWin)
			return new MiniClientWindowsJna();
		if (Configuration.OS.isLinux)
			return new MiniClientLinuxJna();
		if (Configuration.OS.isWin)
			return new MiniClientMacOsJna();
		throw new NotSupportedException(Configuration.OS.name);
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
}
