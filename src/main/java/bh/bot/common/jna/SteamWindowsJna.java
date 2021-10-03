package bh.bot.common.jna;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import bh.bot.common.Configuration;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import bh.bot.common.OS;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.ScreenResolutionProfile.SteamProfile;
import bh.bot.common.types.tuples.Tuple4;

import static bh.bot.common.Log.dev;

public class SteamWindowsJna extends AbstractWindowsJna {
	public SteamWindowsJna() {
		super();
		if (!OS.isWin)
			throw new NotSupportedException(
					String.format("Class %s does not support %s OS", this.getClass().getSimpleName(), OS.name));
	}

	@Override
	public HWND getGameWindow(Object... args) {
		return user32.FindWindow("UnityWndClass", "Bit Heroes");
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd,
			ScreenResolutionProfile screenResolutionProfile) {
		if (!Configuration.isSteamProfile)
			throw new IllegalArgumentException("Not steam profile");

		if (hwnd == null) {
			hwnd = getGameWindow();
			if (hwnd == null)
				return new Tuple4<>(false, "Can not detect Steam window", null, null);
		}

		final int ew = screenResolutionProfile.getSupportedGameResolutionWidth();
		final int eh = screenResolutionProfile.getSupportedGameResolutionHeight();

		RECT lpRectC = new RECT();
		if (!user32.GetClientRect(hwnd, lpRectC))
			return new Tuple4<>(false, String.format("Unable to GetClientRect, err code: %d",
					com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);

		int cw = Math.abs(lpRectC.right - lpRectC.left);
		int ch = Math.abs(lpRectC.bottom - lpRectC.top);
		if (cw <= 0 || ch <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);
		
		Rectangle rect = getRectangle(hwnd);
		if (rect == null)
			return new Tuple4<>(false, String.format("Unable to GetWindowRect, err code: %d",
					com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);

		if (rect.width <= 0 || rect.height <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);
		
		final int ww = rect.width;
		final int wh = rect.height;
		final int borderLeftSize = (ww - cw) / 2;
		final int borderTopSize = wh - ch - borderLeftSize;
		
		if (ew != cw || eh != ch) {
			if (!isSupportResizeWindow() || !resizeWindowToSupportedResolution(hwnd, borderLeftSize * 2 + ew, borderTopSize + borderLeftSize + eh)) {
				return new Tuple4<>(false,
						String.format("JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d", ew,
								eh, cw, ch),
						null, null);
			}

			rect = getRectangle(hwnd);
			if (rect == null)
				return new Tuple4<>(false, String.format("(Post resize) Unable to GetWindowRect, err code: %d",
						com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);
			if (rect.width < ew || rect.height < eh)
				return new Tuple4<>(false,
						String.format("(Post resize) JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d", ew,
								eh, rect.width, rect.height),
						null, null);
			

			lpRectC = new RECT();
			if (!user32.GetClientRect(hwnd, lpRectC))
				return new Tuple4<>(false, String.format("(Post resize) Unable to GetClientRect, err code: %d",
						com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()), null, null);

			cw = Math.abs(lpRectC.right - lpRectC.left);
			ch = Math.abs(lpRectC.bottom - lpRectC.top);
			
			if (ew != cw || eh != ch) {
				return new Tuple4<>(false,
						String.format("(Post resize) JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d", ew,
								eh, cw, ch),
						null, null);
			}
		}
		
		Offset offset = new Offset(rect.x + borderLeftSize, rect.y + borderTopSize);
		if (offset.X < 0 || offset.Y < 0)
			return new Tuple4<>(false,
					String.format("Window may have been partially hiden (x=%d, y=%d)", offset.X, offset.Y), rect,
					offset);

		return new Tuple4<>(true, null, rect, offset);
	}

	@Override
	protected void internalTryToCloseGameWindow() {
		try {
			Process p = Runtime.getRuntime().exec("tasklist");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			String BitHeroesProcess = "Bit Heroes.exe";
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				if (line.contains(BitHeroesProcess)) {
					Runtime.getRuntime().exec(String.format("taskkill /F /IM \"%s\"", BitHeroesProcess));
					return;
				}
			}
		} catch (Exception ignored) {
			//
		}
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final int SWP_NOSIZE = 0x0001;
	@SuppressWarnings("SpellCheckingInspection")
	private static final int SWP_NOMOVE = 0x0002;

	@SuppressWarnings("SpellCheckingInspection")
	private static final HWND HWND_TOPMOST = new HWND(new Pointer(-1));

	@Override
	public void setGameWindowOnTop(HWND hwnd) {
		try {
			User32.INSTANCE.SetWindowPos(hwnd, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);
		} catch (Exception ex) {
			dev("Problem while trying to set game window on top");
			dev(ex);
		}
	}
	
	@Override
	public boolean resizeWindowToSupportedResolution(HWND hwnd, int w, int h) {
        try {
    		return User32.INSTANCE.SetWindowPos(hwnd, HWND_TOPMOST, 0, 0, w, h, SWP_NOMOVE);
        } catch (Exception ex) {
        	dev("Problem while trying to resize game window");
        	dev(ex);
        	return false;
        }
	}
	
	@Override
	public boolean isSupportResizeWindow() {
		return true;
	}
}
