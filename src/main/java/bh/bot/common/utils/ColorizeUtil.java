package bh.bot.common.utils;

import bh.bot.common.Configuration;
import org.fusesource.jansi.Ansi;

import java.util.function.Function;

public class ColorizeUtil {
	public static final Function<Ansi, Ansi> formatInfo = Ansi::fgBrightBlue;
	public static final Function<Ansi, Ansi> formatAsk = Ansi::fgBrightCyan;
	public static final Function<Ansi, Ansi> formatWarning = Ansi::fgBrightYellow;
	public static final Function<Ansi, Ansi> formatError = Ansi::fgBrightRed;
	
	@SuppressWarnings("unused")
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