package bh.bot.common;

import bh.bot.common.types.Offset;
import bh.bot.common.utils.StringUtil;
import com.diogonunes.jcolor.AnsiFormat;

import java.awt.*;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

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

    public static void info(AnsiFormat cFormat, String format, Object... objs) {
        println(color(String.format(format, objs), cFormat));
    }

    private static final AnsiFormat fWarning = new AnsiFormat(YELLOW_TEXT(), BOLD());

    public static void warn(String format, Object... objs) {
        if (StringUtil.isBlank(format))
            return;
        String text = String.format(format, objs);
        println(color(String.format("** WARNING ** %s", text), fWarning));
    }

    private static final AnsiFormat fError = new AnsiFormat(RED_TEXT(), BOLD());

    public static void err(Object obj) {
        if (obj instanceof Exception) {
            ((Exception) obj).printStackTrace();
            return;
        }
        if (obj == null)
            return;
        System.err.println(color(String.format("** ERR ** %s", obj.toString()), fError));
    }

    public static void err(String format, Object... objs) {
        System.err.println(color(String.format("** ERR ** %s", String.format(format, objs)), fError));
    }

    private static void println(Object obj) {
        if (obj == null)
            return;
        System.out.println(obj.toString());
    }

    private static String color(String text, AnsiFormat attributes) {
        if (Configuration.Features.disableColorizeTerminal)
            return text;
        return colorize(text, attributes);
    }
}
