package bh.bot.common.utils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class RegistryUtil {
    public static String readRegistryString(String path, String key) {
        String value = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, path, key);
        return value == null ? null : value.trim();
    }
}
