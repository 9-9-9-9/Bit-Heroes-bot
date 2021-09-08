package bh.bot.common.jna;

import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;

public interface IJna {
	HWND getGameWindow(Object...args);
	Rectangle getRectangle(HWND hwnd);
	Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(HWND hwnd, ScreenResolutionProfile screenResolutionProfile);
	void tryToCloseGameWindow();
}