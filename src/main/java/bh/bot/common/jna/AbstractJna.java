package bh.bot.common.jna;

import static bh.bot.common.Log.err;

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
}