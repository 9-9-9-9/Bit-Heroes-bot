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
	
	public static class Cu {
		private final Ansi ansi = Ansi.ansi();

		private Cu() {
		}

		public Cu blue(String text) {
			ansi.fgBrightBlue().a(text);
			return this;
		}

		public Cu cyan(String text) {
			ansi.fgBrightCyan().a(text);
			return this;
		}

		public Cu green(String text) {
			ansi.fgBrightGreen().a(text);
			return this;
		}

		public Cu magenta(String text) {
			ansi.fgBrightMagenta().a(text);
			return this;
		}

		public Cu red(String text) {
			ansi.fgBrightRed().a(text);
			return this;
		}

		public Cu yellow(String text) {
			ansi.fgBrightYellow().a(text);
			return this;
		}

		public Cu a(String text) {
			ansi.a(text);
			return this;
		}

		public Cu reset() {
			ansi.reset();
			return this;
		}

		public Ansi ansi() {
			return ansi;
		}

		@Override
		public String toString() {
			return ansi.toString();
		}

		public static Cu i() {
			return new Cu();
		}
	}
}