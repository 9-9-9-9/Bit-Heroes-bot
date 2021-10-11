package bh.bot.common.extensions;

import bh.bot.common.utils.RandomUtil;
import org.fusesource.jansi.Ansi;

import java.util.function.Function;

import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;

public class Rad {
    public static void exec(int percent, Runnable runnable) {
        if (percent >= 100) {
            runnable.run();
            return;
        }

        if (percent < 1)
            return;

        if (RandomUtil.nextInt(100) + 1 > percent)
            return;

        runnable.run();
    }

    public static void print(int percent, Function<Ansi, Ansi> formatter, String format, Object...args) {
        exec(percent, () -> info(formatter, format, args));
    }

    public static void print(int percent, String format, Object...args) {
        exec(percent, () -> info(format, args));
    }

    public static void pWarn(int percent, String format, Object...args) {
        exec(percent, () -> warn(format, args));
    }
}
