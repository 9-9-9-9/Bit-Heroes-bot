package bh.bot.common;

import bh.bot.common.types.Offset;
import bh.bot.common.utils.StringUtil;

import java.awt.*;
import java.util.function.Function;

import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;
import static bh.bot.common.utils.ColorizeUtil.*;

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

    public static void optionalDebug(boolean debug, String format, Object... objs) {
        if (!debug)
            return;
        debug(format, objs);
    }

    public static void dev(String format, Object... objs) {
        if (isOnDebugMode || Configuration.enableDevFeatures)
            println(String.format(format, objs));
    }

    public static void printIfIncorrectImgPosition(Offset imCoordinateOffset, Point actualCoordinate) {
        if (!Configuration.enableDevFeatures)
            return;
        int offsetX = actualCoordinate.x - Configuration.gameScreenOffset.X.get();
        int offsetY = actualCoordinate.y - Configuration.gameScreenOffset.Y.get();
        if (imCoordinateOffset.X == offsetX && imCoordinateOffset.Y == offsetY)
            return;
        if (Math.abs(imCoordinateOffset.X - offsetX) < 2 && Math.abs(imCoordinateOffset.Y - offsetY) < 2)
            return;
        try {
            throw new Exception("Un-match offset (for debugging purpose)");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            info("** WARNING ** Un-match offset! Defined %3d,%3d but actual %3d,%3d", imCoordinateOffset.X,
                    imCoordinateOffset.Y, offsetX, offsetY);
        }
    }

    public static void printStackTrace() {
        if (!Configuration.enableDevFeatures)
            return;
        try {
            throw new Exception("Print stack trace for debugging purpose");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void info(Object obj) {
        println(obj);
    }

    public static void info(String format, Object... objs) {
        println(String.format(format, objs));
    }

    public static void info(Function<Ansi, Ansi> fFormat, String format, Object... objs) {
        println(colorize(String.format(format, objs), fFormat));
    }

    public static void warn(String format, Object... objs) {
        if (StringUtil.isBlank(format))
            return;
        String text = String.format(format, objs);
        println(colorize(String.format("** WARNING ** %s", text), formatWarning));
    }

    public static void err(Object obj) {
        if (obj instanceof Exception) {
            ((Exception) obj).printStackTrace();
            return;
        }
        if (obj == null)
            return;
        System.err.println(colorize(String.format("** ERR ** %s", obj.toString()), formatError));
    }

    public static void err(String format, Object... objs) {
        System.err.println(colorize(String.format("** ERR ** %s", String.format(format, objs)), formatError));
    }

    private static void println(Object obj) {
        if (obj == null)
            return;
        System.out.println(obj.toString());
    }

    private static String colorize(String text, Function<Ansi, Ansi> formatter) {
        if (Configuration.Features.disableColorizeTerminal)
            return text;
        return formatter.apply(ansi()).a(text).reset().toString();
    }
    
    
}
