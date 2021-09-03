package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

public class MiniClientMacOsJna extends AbstractMacOSJna {
	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientMacOsJna::getRectangle");
	}

}
