package bh.bot.common.utils;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.Offset;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.images.BwMatrixMeta.Metas;
import bh.bot.common.types.tuples.Tuple4;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.optionalDebug;
import static bh.bot.common.utils.ImageUtil.freeMem;
import static bh.bot.common.utils.ThreadUtil.sleep;

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

		public static void sendEnter() {
			sendKey(KeyEvent.VK_ENTER);
		}

		public static void sendEscape() {
			sendKey(KeyEvent.VK_ESCAPE);
		}
	}

	public static class Mouse {
		public static void moveCursor(Point p) {
			debug("Mouse move cursor %d,%d", p.x, p.y);
			robot.mouseMove(p.x, p.y);
			sleep(5);
		}

		public static void mouseClick() {
			int mask = InputEvent.BUTTON1_DOWN_MASK;
			robot.mousePress(mask);
			sleep(50);
			robot.mouseRelease(mask);
			debug("Mouse click");
		}

		private static final Point pHideCursor = new Point(950, 100);

		public static void mouseMoveAndClickAndHide(Point p) {
			moveCursor(p);
			sleep(300);
			mouseClick();
			sleep(200);
			hideCursor();
		}

		public static void hideCursor() {
			Point hidePoint = new Offset(0, 0).toScreenCoordinate();
			debug("Mouse move cursor %d,%d", hidePoint.x, hidePoint.y);
			moveCursor(hidePoint);
		}

		public static void clickRadioButton(int level, Point[] points, String evName) {
			if (level < 1 || level > points.length)
				throw new InvalidDataException(
						"Can not select level %d/%d of %s because it's not exists, do you setup wrongly?", level, points.length, evName
				);
			Point p = points[level - 1];
			moveCursor(new Point(p.x + 7, p.y + 7));
			sleep(500);
			mouseClick();
			sleep(100);
			hideCursor();
		}
	}

	public static class Screen {
		public static ScreenCapturedResult captureElementInEstimatedArea(BwMatrixMeta im) {
			return captureElementInEstimatedArea(im.getCoordinateOffset(), im.getWidth(), im.getHeight());
		}

		public static ScreenCapturedResult captureElementInEstimatedArea(Offset coordinate, int w,
																		 int h) {
			int xS = Math.max(0,
					Configuration.gameScreenOffset.X.get() + coordinate.X - Configuration.Tolerant.position);
			int yS = Math.max(0,
					Configuration.gameScreenOffset.Y.get() + coordinate.Y - Configuration.Tolerant.position);
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

		public static class ScreenCapturedResult implements AutoCloseable {
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

			@Override
			public void close() {
				freeMem(this.image);
			}
		}

		public static class Game {
			private final AbstractApplication instance;

			private Game(AbstractApplication instance) {
				this.instance = instance;
			}

			public static Game of(AbstractApplication instance) {
				return new Game(instance);
			}

			public Point findAttendablePlace(AttendablePlace event) {
				int minX, maxX, stepY, firstY;
				if (event.left) {
					Tuple4<Integer, Integer, Integer, Integer> backwardScanLeftAttendablePlaces = Configuration.screenResolutionProfile
							.getBackwardScanLeftSideAttendablePlaces();
					minX = backwardScanLeftAttendablePlaces._1;
					firstY = backwardScanLeftAttendablePlaces._2;
					stepY = backwardScanLeftAttendablePlaces._3;
					maxX = backwardScanLeftAttendablePlaces._4;
				} else { // right
					Tuple4<Integer, Integer, Integer, Integer> backwardScanRightAttendablePlaces = Configuration
							.screenResolutionProfile
							.getBackwardScanRightSideAttendablePlaces();
					minX = backwardScanRightAttendablePlaces._1;
					firstY = backwardScanRightAttendablePlaces._2;
					stepY = backwardScanRightAttendablePlaces._3;
					maxX = backwardScanRightAttendablePlaces._4;
				}

				minX += Configuration.gameScreenOffset.X.get();
				maxX += Configuration.gameScreenOffset.X.get();
				firstY += Configuration.gameScreenOffset.Y.get();
				
				final boolean debug = false;

				final int positionTolerant = Math.abs(Math.min(Configuration.Tolerant.position, Math.abs(stepY)));
				final int scanWidth = maxX - minX + 1 + positionTolerant * 2;
				final int scanHeight = Math.abs(stepY) + positionTolerant * 2;
				final int scanX = Math.max(0, minX - positionTolerant);
				final int numberOfAttendablePlacesPerColumn = 5;
				for (int i = 0; i < numberOfAttendablePlacesPerColumn; i++) {
					final int scanY = Math.max(0, firstY + stepY * i - positionTolerant);
					BufferedImage sc = captureScreen(scanX, scanY, scanWidth, scanHeight);
					try {
						instance.saveDebugImage(sc, String.format("findAttendablePlace_%d_", i));
						final BwMatrixMeta im = event.img;
						if (im.throwIfNotAvailable())
							continue;
						//
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
											Configuration.Tolerant.color,
											im.getOriginalPixelPart(px[0], px[1]))) {
										allGood = false;
										optionalDebug(debug, "findAttendablePlace first match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]);
										break;
									}
								}

								if (allGood) {
									for (int[] px : im.getNonBlackPixels()) {
										int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
										if (ImageUtil.areColorsSimilar(//
												blackPixelRgb, //
												srcRgb, //
												Configuration.Tolerant.color)) {
											allGood = false;
											optionalDebug(debug, "findAttendablePlace second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]);
											break;
										}
									}
								}

								if (allGood) {
									go = false;
									p = new Point(scanX + x, scanY + y);
									optionalDebug(debug, "findAttendablePlace result %d, %d", p.x, p.y);
								}
							}
						}

						if (!go)
							return p;
						//

					} finally {
						freeMem(sc);
					}
				}
				return null;
			}

			public ArrayList<Point> findByScanScreen(BwMatrixMeta im, int minX, int maxX, int stepY, int firstY) {
				int maxScreenWidth = 800;
				int maxScreenHeight = 520;
				ArrayList<Point> points = new ArrayList<>();
				Point located = null;
				int currentX = minX;
				int currentMaxX = maxX;
				int step = maxX - minX;
				while ( currentMaxX < maxScreenWidth) {
					int loops = (int) Math.floor((maxScreenHeight - firstY) / stepY);
					located = findByScanColumn(im, currentX, currentMaxX, stepY, firstY, loops);
					currentX += step;
					currentMaxX += step;
					if (located != null) {
						points.add(located);
						located = null;
					}
				}
				return points;
			}

			public Point findByScanColumn(BwMatrixMeta im, int minX, int maxX, int stepY, int firstY, int numberOfScans) {
				minX += Configuration.gameScreenOffset.X.get();
				maxX += Configuration.gameScreenOffset.X.get();
				firstY += Configuration.gameScreenOffset.Y.get();
				
				final boolean debug = false;

				final int positionTolerant = Math.abs(Math.min(Configuration.Tolerant.position, Math.abs(stepY)));
				final int scanWidth = maxX - minX + 1 + positionTolerant * 2;
				final int scanHeight = Math.abs(stepY) + positionTolerant * 2;
				final int scanX = Math.max(0, minX - positionTolerant);
				final int numberOfColumns = Math.min(numberOfScans, 5);
				for (int i = 0; i < numberOfColumns; i++) {
					final int scanY = Math.max(0, firstY + stepY * i - positionTolerant);
					BufferedImage sc = captureScreen(scanX, scanY, scanWidth, scanHeight);
					try {
						instance.saveDebugImage(sc, String.format("scanColumn_%d_%s_", i, im.getImageNameCode()));
						if (im.throwIfNotAvailable())
							continue;
						//
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
											Configuration.Tolerant.color,
											im.getOriginalPixelPart(px[0], px[1]))) {
										allGood = false;
										optionalDebug(debug, "scanColumn first match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]);
										break;
									}
								}

								if (allGood) {
									for (int[] px : im.getNonBlackPixels()) {
										int srcRgb = sc.getRGB(x + px[0], y + px[1]) & 0xFFFFFF;
										if (ImageUtil.areColorsSimilar(//
												blackPixelRgb, //
												srcRgb, //
												Configuration.Tolerant.color)) {
											allGood = false;
											optionalDebug(debug, "scanColumn second match failed at %d,%d (%d,%d)", x + px[0], y + px[1], px[0], px[1]);
											break;
										}
									}
								}

								if (allGood) {
									go = false;
									p = new Point(scanX + x, scanY + y);
									optionalDebug(debug, "scanColumn result %d, %d", p.x, p.y);
								}
							}
						}

						if (!go)
							return p;
						//

					} finally {
						freeMem(sc);
					}
				}
				return null;
			}
		}
	}
}
