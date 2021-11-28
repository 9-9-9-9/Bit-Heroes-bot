package bh.bot.app.dev;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.AttendablePlace;
import bh.bot.common.types.AttendablePlaces;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ColorizeUtil;
import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.StringUtil;
import bh.bot.common.utils.TimeUtil;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.InteractionUtil.Keyboard.sendEnter;
import static bh.bot.common.utils.InteractionUtil.Keyboard.sendSpaceKey;
import static bh.bot.common.utils.InteractionUtil.Mouse.*;
import static bh.bot.common.utils.InteractionUtil.Mouse.hideCursor;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.ThreadUtil.sleep;

@SuppressWarnings("unused")
@AppMeta(code = "dwb", name = "Test Code", dev = true)
public class DualWorldBoss extends AbstractApplication {
	protected InteractionUtil.Screen.Game gameScreenInteractor = InteractionUtil.Screen.Game.of(this);

	private static final int btn1X = 382;
	private static final int btn1Y = 527;
	private static final int btn2X = 1176;
	private static final int btn2Y = 527;
	private static final int btnRestoreX = 284;
	private static final int btnRestoreY = 857;
	private static final Point anchor1 = new Point(btn1X, btn1Y - 400);
	private static final Point btn1 = new Point(btn1X, btn1Y);
	private static final Point anchor2 = new Point(btn2X, btn2Y - 400);
	private static final Point btn2 = new Point(btn2X, btn2Y);
	private static final Point btnRestoreMouse = new Point(btnRestoreX, btnRestoreY);

	@Override
	protected void internalRun(String[] args) {
		Terminal terminal = null;
		try {
			terminal = TerminalBuilder.terminal();
		} catch (IOException e) {
			e.printStackTrace();
		}
		terminal.enterRawMode();
		NonBlockingReader reader = terminal.reader();
		while (true) {
			try {
				info(";. ReGroup");
				info("'. Start");
				info(",. ReGroup 1");
				info(".. ReGroup 2");
				info("/. Start reverse");
				BufferedReader br = Main.getBufferedReader();
				int opt = reader.read();

				if (opt == ';') {
					click1();
					sleep(1000);
					click2();
					sleep(500);
					restoreMouse();
				} else if (opt == ',') {
					click1();
					sleep(500);
					restoreMouse();
				} else if (opt == '.') {
					click2();
					sleep(500);
					restoreMouse();
				} else if (opt == '\'') {
					start(anchor2, btn2, anchor1, btn1);
				} else if (opt == '/') {
					start(anchor1, btn1, anchor2, btn2);
				} else {
					System.out.println("Unknown " + opt);
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private void click1() {
		click(anchor1, btn1);
	}

	private void click2() {
		click(anchor2, btn2);
	}

	private void click(Point anchor, Point move) {
		moveCursor(anchor);
		sleep(100);
		mouseClick();
		sleep(200);
		move1();
		sleep(300);
		moveCursor(move);
		sleep(300);
		mouseClick();
		sleep(200);
		mouseClick();
		sleep(100);
		mouseClick();
	}

	private void start(Point anchorFirst, Point moveFirst, Point anchorSecond, Point moveSecond) {
		click(anchorFirst, moveFirst);
		sleep(3000);
		click(anchorSecond, moveSecond);
		sleep(500);
		sendEnter();
		sleep(500);
		sendEnter();
		sleep(500);
		sendEnter();
		sleep(100);
		restoreMouse();
	}

	private void restoreMouse() {
		moveCursor(btnRestoreMouse);
		sleep(500);
		mouseClick();
		sleep(200);
		mouseClick();
		sleep(200);
		mouseClick();
	}

	private void move1() {
		moveCursor(btn1);
	}

	private void move2() {
		moveCursor(btn2);
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
