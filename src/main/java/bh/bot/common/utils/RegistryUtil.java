package bh.bot.common.utils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class RegistryUtil {
	public static final WinReg.HKEY root = WinReg.HKEY_CURRENT_USER;
	public static final String regKeyBot = "SOFTWARE\\bh99bot";
	public static final String regValueVer = "CurVer";
	public static final String regValueDir = "CurDir";

	public static String readRegistryString(String valueName) {
		if (!Advapi32Util.registryValueExists(root, regKeyBot, valueName))
			return null;
		String value = Advapi32Util.registryGetStringValue(root, regKeyBot, valueName);
		return value == null ? null : value.trim();
	}

	public static void prepareRegKey() {
		if (Advapi32Util.registryKeyExists(root, regKeyBot))
			return;
		Advapi32Util.registryCreateKey(root, "SOFTWARE", "bh99bot");
	}
	
	public static void createValue(String valueName, String value) {
		Advapi32Util.registrySetStringValue(root, regKeyBot, valueName, value);
	}
}
