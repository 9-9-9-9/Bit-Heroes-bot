package bh.bot.common.jna;

import static bh.bot.common.Log.err;

import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.exceptions.NotSupportedException;

public abstract class AbstractJna implements IJna {
    @Override
    public final void tryToCloseGameWindow() {
        try {
            internalTryToCloseGameWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Failure on trying to close game window");
        }
    }

    protected abstract void internalTryToCloseGameWindow() throws Exception;
    
    @Override
    public void setGameWindowOnTop(HWND hwnd) {
    	throw new NotSupportedException("Method setGameWindowOnTop has not been implemented for class " + this.getClass().getSimpleName());
    }
    
    @Override
    public boolean resizeWindowToSupportedResolution(HWND hwnd, int w, int h) {
    	throw new NotSupportedException("Method resizeWindowToSupportedResolution has not been implemented for class " + this.getClass().getSimpleName());
    }
    
    @Override
    public boolean isSupportResizeWindow() {
    	return false;
    }
}