package bh.bot.app.dev;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Configuration.Offset;
import bh.bot.common.jna.IJna;
import bh.bot.common.jna.MiniClientWindowsJna;
import bh.bot.common.jna.SteamWindowsJna;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.InteractionUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.*;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.ThreadUtil.sleep;

@SuppressWarnings("unused")
@AppCode(code = "test")
public class TestApp extends AbstractApplication {
	protected InteractionUtil.Screen.Game gameScreenInteractor = InteractionUtil.Screen.Game.of(this);

	@Override
	protected void internalRun(String[] args) {
		adjustScreenOffset();

		String str1, str2;
		BufferedReader br = Main.getBufferedReader();
		str1 = readInput(br, "ask1", null, s -> new Tuple3<>(true, null, "1"));
		str2 = readInput(br, "ask2", null, s -> new Tuple3<>(true, null, "2"));
	}

	public static class JnaTest {
		public static Rectangle getWindowLocationAndSize(final HWND hwnd) {
			final RECT lpRect = new RECT();
			if (!User32.INSTANCE.GetWindowRect(hwnd, lpRect))
				throw new Win32Exception(com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError());
			return new Rectangle(lpRect.left, lpRect.top, Math.abs(lpRect.right - lpRect.left),
					Math.abs(lpRect.bottom - lpRect.top));
		}
	}

	@SuppressWarnings("unused")
	private void detectRaidRadioButtons() {
		Tuple2<Point[], Byte> result = detectRadioButtons(
				Configuration.screenResolutionProfile.getRectangleRadioButtonsOfRaid());
		Point[] points = result._1;
		byte selectedIndex = result._2;
		info("Found %d, selected %d", points.length, selectedIndex + 1);
		clickRadioButton(3, points, "Raid");
	}

	@SuppressWarnings("unused")
	private void detectWorldBossRadioButtons() {
		Tuple2<Point[], Byte> result = detectRadioButtons(
				Configuration.screenResolutionProfile.getRectangleRadioButtonsOfWorldBoss());
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
	protected String getAppName() {
		return "BH-Test code";
	}

	@Override
	protected String getScriptFileName() {
		return "test";
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
