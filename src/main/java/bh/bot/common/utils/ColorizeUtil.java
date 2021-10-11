package bh.bot.common.utils;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.function.Function;

import bh.bot.common.Configuration;
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
		private final StringBuilder sb = new StringBuilder();

		private Cu() {
		}

		public Cu blue(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightBlue().a(text);
			return this;
		}

		public Cu cyan(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightCyan().a(text);
			return this;
		}

		public Cu green(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightGreen().a(text);
			return this;
		}

		public Cu magenta(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightMagenta().a(text);
			return this;
		}

		public Cu red(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightRed().a(text);
			return this;
		}

		public Cu yellow(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.fgBrightYellow().a(text);
			return this;
		}

		public Cu a(String text) {
			if (Configuration.Features.disableColorizeTerminal)
				sb.append(text);
			else
				ansi.a(text);
			return this;
		}

		public Cu reset() {
			if (!Configuration.Features.disableColorizeTerminal)
				ansi.reset();
			return this;
		}

		public Cu ra(String text) {
			return this.reset().a(text);
		}

		public Ansi ansi() {
			return ansi;
		}

		@Override
		public String toString() {
			return Configuration.Features.disableColorizeTerminal ? sb.toString() : ansi.toString();
		}

		public static Cu i() {
			return new Cu();
		}
	}
}