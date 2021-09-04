package bh.bot.common;

import bh.bot.common.types.Platform;

public class OS {
    public static final String name = System.getProperty("os.name");
    public static final String normalizedName = name.trim().toLowerCase();
    public static final boolean isMac = normalizedName.indexOf("mac") >= 0 || normalizedName.indexOf("darwin") >= 0;
    public static final boolean isWin = !isMac && normalizedName.indexOf("win") >= 0;
    public static final boolean isLinux = !isMac && !isWin;
    public static Platform platform = isWin ? Platform.Windows
            : isMac ? Platform.MacOS : isLinux ? Platform.Linux : Platform.Unknown;
}