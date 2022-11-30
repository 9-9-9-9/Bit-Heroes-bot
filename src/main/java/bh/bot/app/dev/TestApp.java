package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.jna.IJna;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.Offset;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.images.BufferedImageInfo;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.ImageUtil.TestTransformMxResult;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.TimeUtil;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.clickRadioButton;

@SuppressWarnings("unused")
@AppMeta(code = "test", name = "Test Code", dev = true)
public class TestApp extends AbstractApplication {
	protected InteractionUtil.Screen.Game gameScreenInteractor = InteractionUtil.Screen.Game.of(this);

	@Override
	protected void internalRun(String[] args) {
		adjustScreenOffset();

		//
		Tuple2<Point[], Byte> result = detectRadioButtons(
				Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid()
		);
		Point[] points = result._1;
		int selectedLevel = result._2 + 1;
		info("Selected %d", selectedLevel);
		for (int i = 0; i < points.length; i++) {
			Point p = points[i];
			info("[%d] %d,%d", i + 1, p.x - Configuration.gameScreenOffset.X.get(), p.y - Configuration.gameScreenOffset.Y.get());
		}
	}

	private static void testParseTime() {
		assertTestTime("1d3m", 86400 + 3 * 60);
		assertTestTime("2d", 86400 * 2);
		assertTestTime("3h5m5", 3 * 3600 + 5 * 60 + 5);
		assertTestTime("3h5m5s", 3 * 3600 + 5 * 60 + 5);
		assertTestTime("6h5s", 6 * 3600 + 5);
		assertTestTime("5d2", 5 * 86400 + 2);
		assertTestTime("5d2", 5 * 86400 + 3);
		assertTestTime("5m2", 5 * 60 + 2);
		assertTestTime("5m2s", 5 * 60 + 2);
		assertTestTime("5z2s", 5 * 60 + 2);
		info("OK");
	}

	private static void assertTestTime(String input, int expect) {
		try {
			if (TimeUtil.parseTimeToSec(input) != expect)
				err("%10s != %-7d", input, expect);
		} catch (InvalidDataException ex) {
			err("Failure compare %10s vs %-7d: %s", input, expect, ex.getMessage());
		}
	}

	public static class JnaTest {
		public static Rectangle getWindowLocationAndSize(final HWND hwnd) {
			final RECT lpRect = new RECT();
			if (!User32.INSTANCE.GetWindowRect(hwnd, lpRect))
				throw new Win32Exception(com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError());
			return new Rectangle(
				lpRect.left, lpRect.top, 
				Math.abs(lpRect.right - lpRect.left), Math.abs(lpRect.bottom - lpRect.top)
			);
		}
	}

	@SuppressWarnings("unused")
	private void detectRaidRadioButtons() {
		Tuple2<Point[], Byte> result = detectRadioButtons(
			Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid()
		);
		Point[] points = result._1;
		byte selectedIndex = result._2;
		info("Found %d, selected %d", points.length, selectedIndex + 1);
		clickRadioButton(3, points, "Raid");
	}

	@SuppressWarnings("unused")
	private void detectWorldBossRadioButtons() {
		Tuple2<Point[], Byte> result = detectRadioButtons(
			Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss()
		);
		Point[] points = result._1;
		byte selectedIndex = result._2;
		info("Found %d, selected %d", points.length, selectedIndex + 1);
		clickRadioButton(3, points, "World Boss");
	}

	@SuppressWarnings("unused")
	private void findAttendablePlaces() {
		final List<AttendablePlace> allAttendablePlaces = Arrays.asList(//
				AttendablePlaces.invasion, //
				AttendablePlaces.expedition, //
				AttendablePlaces.trials, //
				AttendablePlaces.gvg, //
				AttendablePlaces.gauntlet, //
				AttendablePlaces.pvp, //
				AttendablePlaces.worldBoss, //
				AttendablePlaces.raid //
		);
		allAttendablePlaces.forEach(ap -> {
			Point point = this.gameScreenInteractor.findAttendablePlace(ap);
			if (point != null) {
				info("%3d, %3d: %s", point.x, point.y, ap.name);
			} else {
				info("Not found %s", ap.name);
			}
		});
	}

	@Override
	protected String getUsage() {
		return null;
	}

	@Override
	protected String getDescription() {
		return "(developers only) run test code";
	}

	@Override
	protected String getLimitationExplain() {
		return null;
	}
}
