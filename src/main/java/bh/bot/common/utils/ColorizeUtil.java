package bh.bot.common.utils;

import static org.fusesource.jansi.Ansi.ansi;

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
}