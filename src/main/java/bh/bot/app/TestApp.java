package bh.bot.app;

import bh.bot.common.Configuration;
import bh.bot.common.types.annotations.AppCode;

import static bh.bot.common.Log.err;

/*
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import static bh.bot.common.Log.info;
*/

@AppCode(code = "test")
public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        if (!Configuration.OS.isWin) {
            err("For Windows only");
            return;
        }

        /*

        CMath lib = Native.load(Platform.isWindows()?"msvcrt":"c", CMath.class);
        double result = lib.cosh(0);
        info(result);

        String startOfWindowName = "Bit Heroes";
        Pointer hWnd = JnaUtil.getWinHwnd(startOfWindowName);
        if (hWnd == null || startOfWindowName.isEmpty()) {
            String message = String.format("Window named \"%s\" was not found", startOfWindowName);
            err(message);
            return;
        }

        int[] rect = {0, 0, 0, 0};
        result = User32.INSTANCE.GetWindowRect(hWnd, rect);
        if (result == 0) {
            err("result == 0");
            return;
        }

        JnaUtil.moveWindow(hWnd, 0, 0, rect[2], rect[3] + 40);
         */
    }

    /*
    static class JnaUtil {
        private static final User32 user32 = User32.INSTANCE;
        private static Pointer callBackHwnd;

        public static Pointer getWinHwnd(final String startOfWindowName) {
            callBackHwnd = null;

            user32.EnumWindows(new User32.WNDENUMPROC() {
                @Override
                public boolean callback(Pointer hWnd, Pointer userData) {
                    byte[] windowText = new byte[512];
                    user32.GetWindowTextA(hWnd, windowText, 512);
                    String wText = Native.toString(windowText).trim();

                    if (!wText.isEmpty() && wText.startsWith(startOfWindowName)) {
                        callBackHwnd = hWnd;
                        return false;
                    }
                    return true;
                }
            }, null);
            return callBackHwnd;
        }

        public static boolean moveWindow(Pointer hWnd, int x, int y, int nWidth,
                                         int nHeight) {
            boolean bRepaint = true;
            return user32.MoveWindow(hWnd, x, y, nWidth, nHeight, bRepaint);
        }

    }

    interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }

        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

        boolean MoveWindow(Pointer hWnd, int x, int y, int nWidth, int nHeight,
                           boolean bRepaint);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);

        int GetWindowRect(Pointer handle, int[] rect);

    }

    public interface CMath extends Library {
        double cosh(double value);
    }
    */

    @Override
    protected String getAppName() {
        return "BH-Test code";
    }

    @Override
    protected String getScriptFileName() {
        return "test";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "(developers only) run test code";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
