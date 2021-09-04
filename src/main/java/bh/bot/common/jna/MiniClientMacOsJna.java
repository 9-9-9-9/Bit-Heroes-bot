package bh.bot.common.jna;

import bh.bot.common.exceptions.NotImplementedException;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;

public class MiniClientMacOsJna extends AbstractMacOSJna {
	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientMacOsJna::getRectangle");
	}
}