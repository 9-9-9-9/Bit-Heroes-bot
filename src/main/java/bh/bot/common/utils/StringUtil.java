package bh.bot.common.utils;

public class StringUtil {
	public static boolean isBlank(String text) {
		return text == null || text.trim().length() == 0;
	}

	public static boolean isNotBlank(String text) {
		return !isBlank(text);
	}

	public static boolean isTrue(String text) {
		if (text == null)
			return false;
		text = text.trim().toLowerCase();
		return text == "true" || text == "yes" || text == "y";
	}
}