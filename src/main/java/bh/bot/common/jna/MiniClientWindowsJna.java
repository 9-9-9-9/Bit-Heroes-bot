package bh.bot.common.jna;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static bh.bot.common.Log.warn;
import static bh.bot.common.Log.debug;

public class MiniClientWindowsJna extends AbstractWindowsJna {

	@Override
	public DesktopWindow getGameWindow(Object... args) {
		final AtomicReference<DesktopWindow> result = new AtomicReference<>();
		boolean success = false;
		List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
		for (int i = 0; i < windows.size(); i++) {
			DesktopWindow w = windows.get(i);
			char[] textBuffer = new char[1000];
			user32.GetClassName(w.getHWND(), textBuffer, textBuffer.length);
			String className = new String(textBuffer).trim();
			String windowTitle = w.getTitle();
			if ("Bit Heroes".equals(windowTitle)) {
				if ("Chrome_WidgetWin_1".equals(className)) {
					result.set(w);
					success = true;
					break;
				}
			}
		}
		
		return success ? result.get() : null;
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(DesktopWindow desktopWindow, ScreenResolutionProfile screenResolutionProfile) {
		if (Configuration.isSteamProfile)
			throw new IllegalArgumentException("Does not support steam profile");

		if (desktopWindow == null) {
			desktopWindow = getGameWindow();
			if (desktopWindow == null)
				return new Tuple4<>(false, "Can not detect mini client window", null, null);
		}
		this.setGameWindowOnTop(desktopWindow);
		Rectangle rect = desktopWindow.getLocAndSize();
		
		if (rect.width <= 0 || rect.height <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);

		Offset offset = new Offset(rect.x+10, rect.y+32);
		if (offset.X < 0 || offset.Y < 0)
			Main.showWarningWindowMustClearlyVisible();

		debug("Rect and Offset " + rect.toString() + " " + offset.toScreenCoordinate().toString());
		return new Tuple4<>(true, null, rect, offset);
	}

	@Override
	protected void internalTryToCloseGameWindow() {
		warn("tryToCloseGameWindow: This feature is not yet implemented for mini-client on Windows");
	}

	@Override
	public void setGameWindowOnTop(DesktopWindow desktopWindow) {
		if (user32.SetForegroundWindow(desktopWindow.getHWND())) {
			user32.SetFocus(desktopWindow.getHWND());
		}
	}
}
