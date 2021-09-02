package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.*;

public interface IJna {
	HWND getGameWindow(Object...args);
	Rectangle getRectangle(HWND hwnd);
	Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd, ScreenResolutionProfile screenResolutionProfile);
}