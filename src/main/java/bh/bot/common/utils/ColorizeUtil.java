package bh.bot.common.utils;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.function.Function;

import org.fusesource.jansi.Ansi;

public class ColorizeUtil {
	public static String cInfo(String text) {
		return ansi().fgBrightBlue().a(text).reset().toString();
	}

	public static String cAsk(String text) {
		return ansi().fgBrightCyan().a(text).reset().toString();
	}

	public static String cWarning(String text) {
		return ansi().fgBrightYellow().a(text).reset().toString();
	}

	public static String cError(String text) {
		return ansi().fgBrightRed().a(text).reset().toString();
	}
	
	public static final Function<Ansi, Ansi> formatInfo = ansi -> ansi.fgBrightBlue();
	
	public static final Function<Ansi, Ansi> formatAsk = ansi -> ansi.fgBrightCyan();
	
	public static final Function<Ansi, Ansi> formatWarning = ansi -> ansi.fgBrightYellow();
	
	public static final Function<Ansi, Ansi> formatError = ansi -> ansi.fgBrightRed();
}