package bh.bot.common.jna;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.ScreenResolutionProfile.SteamProfile;
import bh.bot.common.types.tuples.Tuple4;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class MiniClientWindowsJna extends AbstractWindowsJna {

	@Override
	public HWND getGameWindow(Object... args) {
		HWND hwnd = user32.FindWindow("Chrome_WidgetWin_1", "Bit Heroes");
		final AtomicReference<HWND> result = new AtomicReference<>();
		boolean success = user32.EnumChildWindows(hwnd, (hWnd, data) -> {
			char[] textBuffer = new char[1000];
			user32.GetClassName(hWnd, textBuffer, textBuffer.length);
			String className = new String(textBuffer).trim();
			if ("Chrome_RenderWidgetHostHWND".equals(className)) {
				result.set(hWnd);
				return true;
			}
			return false;
		}, null);
		return success ? result.get() : null;
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd,
			ScreenResolutionProfile screenResolutionProfile) {
		if (screenResolutionProfile instanceof SteamProfile)
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
			return new Tuple4<>(false,
					String.format("Window may have been partially hiden (x=%d, y=%d)", offset.X, offset.Y), rect,
					offset);

		return new Tuple4<>(true, null, rect, offset);
	}
}
