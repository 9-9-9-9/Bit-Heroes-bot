package bh.bot.common;

import bh.bot.common.utils.StringUtil;

import java.awt.*;

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

    public static void dev(String format, Object... objs) {
        if (isOnDebugMode || Configuration.enableDevFeatures)
            println(String.format(format, objs));
    }

    public static void printIfIncorrectImgPosition(Configuration.Offset imCoordinateOffset, Point actualCoordinate) {
        if (!Configuration.enableDevFeatures)
            return;
        int offsetX = actualCoordinate.x - Configuration.gameScreenOffset.X;
        if (imCoordinateOffset.X == offsetX && imCoordinateOffset.Y == actualCoordinate.y - Configuration.gameScreenOffset.Y)
            return;
        try {
            throw new Exception("Un-match offset (for debugging purpose)");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            info(
                    "** WARNING ** Un-match offset! Defined %3d,%3d but actual %3d,%3d",
                    imCoordinateOffset.X, imCoordinateOffset.Y,
                    offsetX, actualCoordinate.y - Configuration.gameScreenOffset.Y
            );
        }
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
