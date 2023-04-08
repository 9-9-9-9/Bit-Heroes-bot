package bh.bot.common.jna;

import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;

import com.sun.jna.platform.DesktopWindow;
import java.awt.*;

public interface IJna {
	DesktopWindow getGameWindow(Object...args);
	Rectangle getRectangle(DesktopWindow desktopWindow);
	Tuple4<Boolean, String, Rectangle, Offset> locateGameScreenOffset(DesktopWindow desktopWindow, ScreenResolutionProfile screenResolutionProfile);
	void tryToCloseGameWindow();
	void setGameWindowOnTop(DesktopWindow desktopWindow);
	boolean resizeWindowToSupportedResolution(DesktopWindow desktopWindow, int w, int h);
	boolean isSupportResizeWindow();
}