package bh.bot.common;

import bh.bot.common.utils.StringUtil;

public class Log {
    private static boolean isOnDebugMode;

    public static void enableDebug() {
        isOnDebugMode = true;
        info("Enabled debug messages");
    }

    public static void debug(Object obj) {
        if (!isOnDebugMode)
            return;
        println(obj);
    }

    public static void debug(String format, Object... objs) {
        if (!isOnDebugMode)
            return;
        println(String.format(format, objs));
    }

    public static void info(Object obj) {
        println(obj);
    }

    public static void info(String format, Object... objs) {
        println(String.format(format, objs));
    }

    public static void warn(String format, Object... objs) {
        if (StringUtil.isBlank(format))
            return;
        String text = String.format(format, objs);
        println(String.format("** WARNING ** %s", text));
    }

    public static void err(Object obj) {
        if (obj instanceof Exception) {
            ((Exception) obj).printStackTrace();
            return;
        }
        System.err.println(obj);
    }

    public static void err(String format, Object... objs) {
        System.err.println(String.format(format, objs));
    }

    private static void println(Object obj) {
        if (obj == null)
            return;
        System.out.println(obj.toString());
    }
}
