package bh.bot.common.jna;

import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;

import static bh.bot.common.Log.warn;

public class MiniClientMacOsJna extends AbstractMacOSJna {
	@Override
	public Rectangle getRectangle(DesktopWindow desktopWindow) {
		if (desktopWindow == null) {
            return null;
        }
        return desktopWindow.getLocAndSize();
	}

	@Override
	protected void internalTryToCloseGameWindow() {
		warn("tryToCloseGameWindow: This feature is not yet implemented for mini-client on MacOS");
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(DesktopWindow desktopWindow, ScreenResolutionProfile screenResolutionProfile) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'locateGameScreenOffset'");
	}
}