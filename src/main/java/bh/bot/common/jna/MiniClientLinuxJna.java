package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

public class MiniClientLinuxJna extends AbstractLinuxJna {

	@Override
	public HWND getGameWindow(Object... args) {
		throw new NotImplementedException("MiniClientLinuxJna::getGameWindow");
	}

	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientLinuxJna::getRectangle");
	}

	@Override
	public Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd,
			ScreenResolutionProfile screenResolutionProfile) {
		throw new NotImplementedException("MiniClientLinuxJna::locateGameScreenOffset");
	}

}
