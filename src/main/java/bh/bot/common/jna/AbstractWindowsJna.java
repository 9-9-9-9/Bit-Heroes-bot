package bh.bot.common.jna;

import com.sun.jna.platform.win32.User32;

public abstract class AbstractWindowsJna extends AbstractJna {
    protected final User32 user32 = User32.INSTANCE;
}
