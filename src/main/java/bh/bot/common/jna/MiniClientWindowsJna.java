package bh.bot.common.jna;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.warn;

public class MiniClientWindowsJna extends AbstractWindowsJna {

	@Override
	public HWND getGameWindow(Object... args) {
		final AtomicReference<HWND> result = new AtomicReference<>();
		boolean success = false;
		List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
		for (int i = 0; i < windows.size(); i++) {
			DesktopWindow w = windows.get(i);
			char[] textBuffer = new char[1000];
			user32.GetClassName(w.getHWND(), textBuffer, textBuffer.length);
			String className = new String(textBuffer).trim();
			String windowTitle = w.getTitle();
			debug("" + windowTitle + " | " + className);
			if ("Bit Heroes".equals(windowTitle)) {
				if ("Chrome_WidgetWin_1".equals(className)) {
					success = user32.EnumChildWindows(w.getHWND(), (hWnd, data) -> {
						char[] innerTextBuffer = new char[1000];
						user32.GetClassName(hWnd, innerTextBuffer, innerTextBuffer.length);
						String innerClassName = new String(innerTextBuffer).trim();
						debug("" + windowTitle + " | " + innerClassName);
						if ("Chrome_RenderWidgetHostHWND".equals(innerClassName) || "Intermediate D3D Window".equals(innerClassName)) {
							result.set(hWnd);
							return true;
						}
						return false;
					}, null);
				}
				break;
			}
		}
		
		return success ? result.get() : null;
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd,
																			 ScreenResolutionProfile screenResolutionProfile) {
		if (Configuration.isSteamProfile)
			throw new IllegalArgumentException("Does not support steam profile");

		if (hwnd == null) {
			hwnd = getGameWindow();
			if (hwnd == null)
				return new Tuple4<>(false, "Can not detect mini client window", null, null);
		}
		
		Rectangle rect = getRectangle(hwnd);
		
		if (rect.width <= 0 || rect.height <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);

		Offset offset = new Offset(rect.x, rect.y);
		if (offset.X < 0 || offset.Y < 0)
			Main.showWarningWindowMustClearlyVisible();

		return new Tuple4<>(true, null, rect, offset);
	}

	@Override
	protected void internalTryToCloseGameWindow() {
		warn("tryToCloseGameWindow: This feature is not yet implemented for mini-client on Windows");
	}
}
