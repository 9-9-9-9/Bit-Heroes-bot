package bh.bot.common.jna;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import bh.bot.Main;
import bh.bot.common.Configuration;
import com.sun.jna.Pointer;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import bh.bot.common.OS;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

import static bh.bot.common.Log.*;

public class SteamWindowsJna extends AbstractWindowsJna {
	public SteamWindowsJna() {
		super();
		if (!OS.isWin)
			throw new NotSupportedException(
					String.format("Class %s does not support %s OS", this.getClass().getSimpleName(), OS.name));
	}

	@Override
	public DesktopWindow getGameWindow(Object... args) {
		List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
		for (int i = 0; i < windows.size(); i++) {
			DesktopWindow w = windows.get(i);
			char[] textBuffer = new char[1000];
					user32.GetClassName(w.getHWND(), textBuffer, textBuffer.length);
			String className = new String(textBuffer).trim();
			String windowTitle = w.getTitle();
			if ("Bit Heroes".equals(windowTitle)) {
				if ("UnityWndClass".equals(className)) {
					return w;
				}
			}
		}
		err("Can not detect game window (Steam)!!!");
		showErrAskIfBhRunningOrReqAdm();
		Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
		return null;
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(DesktopWindow desktopWindow, ScreenResolutionProfile screenResolutionProfile) {
		if (!Configuration.isSteamProfile)
			throw new IllegalArgumentException("Not steam profile");

		if (desktopWindow == null)
		desktopWindow = getGameWindow();

		HWND hwnd = desktopWindow.getHWND();

		final int ew = screenResolutionProfile.getSupportedGameResolutionWidth();
		final int eh = screenResolutionProfile.getSupportedGameResolutionHeight();

		RECT lpRectC = new RECT();
		if (!user32.GetClientRect(hwnd, lpRectC)) {
			err(
					String.format("Unable to GetClientRect, err code: %d",
					com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError())
			);
			showErrAskIfBhRunningOrReqAdm();
			Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
		}

		int cw = Math.abs(lpRectC.right - lpRectC.left);
		int ch = Math.abs(lpRectC.bottom - lpRectC.top);
		if (cw <= 0 || ch <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);
		
		Rectangle rect = desktopWindow.getLocAndSize();
		if (rect == null) {
			err(
					String.format("Unable to GetWindowRect, err code: %d",
							com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError())
			);
			showErrAskIfBhRunningOrReqAdm();
			Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
			return null;
		}

		if (rect.width <= 0 || rect.height <= 0)
			return new Tuple4<>(false, "Window has minimized", null, null);
		
		final int ww = rect.width;
		final int wh = rect.height;
		final int borderLeftSize = (ww - cw) / 2;
		final int borderTopSize = wh - ch - borderLeftSize;
		
		if (ew != cw || eh != ch) {
			if (!isSupportResizeWindow() || !resizeWindowToSupportedResolution(desktopWindow, borderLeftSize * 2 + ew, borderTopSize + borderLeftSize + eh)) {
				err(
						String.format("JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d",
								ew, eh, cw, ch)
				);
				err("Unable to resize window!!!");
				showErrReqAdm();
				Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
			}

			Main.showWarningWindowMustClearlyVisible();

			rect = desktopWindow.getLocAndSize();
			if (rect == null) {
				err(
						String.format(
								"(Post-Resize) Unable to GetWindowRect, err code: %d",
								com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()
						)
				);
				showErrAskIfBhRunningOrReqAdm();
				Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
				return null;
			}

			if (rect.width < ew || rect.height < eh) {
				err(
						String.format(
								"(Post-Resize) JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d",
								ew, eh, rect.width, rect.height
						)
				);
				showErrReqAdm();
				Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
			}

			lpRectC = new RECT();
			if (!user32.GetClientRect(hwnd, lpRectC)) {
				err(
						String.format(
								"(Post resize) Unable to GetClientRect, err code: %d",
							com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError()
						)
				);
				showErrAskIfBhRunningOrReqAdm();
				Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
			}

			cw = Math.abs(lpRectC.right - lpRectC.left);
			ch = Math.abs(lpRectC.bottom - lpRectC.top);
			
			if (ew != cw || eh != ch) {
				err(
						String.format(
								"(Post resize) JNA detect invalid screen size of Steam client! Expected %dx%d but found %dx%d",
								ew, eh, cw, ch
						)
				);
				showErrAskIfBhRunningOrReqAdm();
				Main.exit(Main.EXIT_CODE_WINDOW_DETECTION_ISSUE);
			}
		}
		
		Offset offset = new Offset(rect.x + borderLeftSize, rect.y + borderTopSize);
		if (offset.X < 0 || offset.Y < 0)
			Main.showWarningWindowMustClearlyVisible();

		return new Tuple4<>(true, null, rect, offset);
	}

	private void showErrAskIfBhRunningOrReqAdm() {
		err("Is the Bit Heroes running? If yes, probably this err caused by lacking privilege, you may need to run this bot as administrator");
	}

	private void showErrReqAdm() {
		err("Probably this err caused by lacking privilege, you may need to run this bot as administrator");
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
	public void setGameWindowOnTop(DesktopWindow desktopWindow) {
		try {
			User32.INSTANCE.SetWindowPos(desktopWindow.getHWND(), HWND_TOPMOST, 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);
		} catch (Exception ex) {
			dev("Problem while trying to set game window on top");
			dev(ex);
		}
	}
	
	@Override
	public boolean resizeWindowToSupportedResolution(DesktopWindow desktopWindow, int w, int h) {
        try {
    		return User32.INSTANCE.SetWindowPos(desktopWindow.getHWND(), HWND_TOPMOST, 0, 0, w, h, SWP_NOMOVE);
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
