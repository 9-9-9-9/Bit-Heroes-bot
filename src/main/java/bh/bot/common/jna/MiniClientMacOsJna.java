package bh.bot.common.jna;

import bh.bot.common.exceptions.NotImplementedException;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;

import static bh.bot.common.Log.warn;

public class MiniClientMacOsJna extends AbstractMacOSJna {
	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientMacOsJna::getRectangle");
	}

	@Override
	protected void internalTryToCloseGameWindow() {
		warn("tryToCloseGameWindow: This feature is not yet implemented for mini-client on MacOS");
	}
}