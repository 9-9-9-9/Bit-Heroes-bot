package bh.bot.common.jna;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.OS;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.ScreenResolutionProfile.SteamProfile;
import bh.bot.common.types.tuples.Tuple4;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import java.awt.*;

public class SteamWindowsJna extends AbstractWindowsJna {
	public SteamWindowsJna() {
		super();
		if (!OS.isWin)
			throw new NotSupportedException(String.format("Class %s does not support %s OS",
					this.getClass().getSimpleName(), OS.name));
	}

	@Override
	public HWND getGameWindow(Object... args) {
		return user32.FindWindow("UnityWndClass", "Bit Heroes");
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd,
			ScreenResolutionProfile screenResolutionProfile) {
		if (!(screenResolutionProfile instanceof SteamProfile))
			throw new IllegalArgumentException("Not steam profile");

		if (hwnd == null) {
			hwnd = getGameWindow();
			if (hwnd == null)
				return new Tuple4<>(false, "Can not detect Steam window", null, null);
		}

		final RECT lpRectC = new RECT();
		if (!user32.GetClientRect(hwnd, lpRectC))
			return new Tuple4<>(false, String.format("Unable to GetClientRect, err code: %d",
					com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);

		final int cw = Math.abs(lpRectC.right - lpRectC.left);
		final int ch = Math.abs(lpRectC.bottom - lpRectC.top);
		if (cw <= 0 || ch <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);

		final int ew = screenResolutionProfile.getSupportedGameResolutionWidth();
		final int eh = screenResolutionProfile.getSupportedGameResolutionHeight();
		if (ew != cw || eh != ch)
			return new Tuple4<>(false,
					String.format("JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d", ew,
							eh, cw, ch),
					null, null);

		Rectangle rect = getRectangle(hwnd);
		if (rect == null)
			return new Tuple4<>(false, String.format("Unable to GetWindowRect, err code: %d",
					com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);
		
		if (rect.width <= 0 || rect.height <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);
		if (rect.width < ew || rect.height < eh)
			return new Tuple4<>(false,
					String.format("JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d", ew,
							eh, cw, ch),
					null, null);
		final int ww = rect.width;
		final int wh = rect.height;
		final int borderLeftSize = (ww - cw) / 2;
		final int borderTopSize = wh - ch - borderLeftSize;
		Offset offset = new Offset(rect.x + borderLeftSize, rect.y + borderTopSize);
		if (offset.X < 0 || offset.Y < 0)
			return new Tuple4<>(false,
					String.format("Window may have been partially hiden (x=%d, y=%d)", offset.X, offset.Y), rect,
					offset);

		return new Tuple4<>(true, null, rect, offset);
	}
}
