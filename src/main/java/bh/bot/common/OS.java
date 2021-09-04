package bh.bot.common;

import bh.bot.common.types.Platform;

public class OS {
    public static final String name = System.getProperty("os.name");
    private static final String normalizedName = name.trim().toLowerCase();
    public static final boolean isMac = normalizedName.contains("mac") || normalizedName.contains("darwin");
    public static final boolean isWin = !isMac && normalizedName.contains("win");
    public static final boolean isLinux = !isMac && !isWin;
    @SuppressWarnings("ConstantConditions")
    public static Platform platform = //
            isWin //
                    ? Platform.Windows //
                    : isMac //
                        ? Platform.MacOS //
                        : isLinux //
                            ? Platform.Linux //
                            : Platform.Unknown;
}