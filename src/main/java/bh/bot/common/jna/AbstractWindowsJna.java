package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

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
    public Rectangle getRectangle(HWND hwnd) {
    	if (hwnd == null)
    		return null;
    	
		final RECT lpRectW = new RECT();
		if (!user32.GetWindowRect(hwnd, lpRectW))
			return null;

		return new Rectangle(
			lpRectW.left, 
			lpRectW.top, 
			Math.abs(lpRectW.right - lpRectW.left),
			Math.abs(lpRectW.bottom - lpRectW.top)
		);
    }
}
