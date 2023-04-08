package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.User32;

import bh.bot.common.OS;
import bh.bot.common.exceptions.NotSupportedException;

public abstract class AbstractWindowsJna extends AbstractJna {
    protected final User32 user32 = User32.INSTANCE;

	protected AbstractWindowsJna() {
		if (!OS.isWin)
			throw new NotSupportedException(String.format("Class %s does not support %s OS",
					this.getClass().getSimpleName(), OS.name));
	}
    
    @Override
    public Rectangle getRectangle(DesktopWindow desktopWindow) {
    	if (desktopWindow == null)
    		return null;
    	
		return desktopWindow.getLocAndSize();
    }
}
