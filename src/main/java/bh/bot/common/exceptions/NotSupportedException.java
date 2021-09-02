package bh.bot.common.exceptions;

public class NotSupportedException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8411478604990797549L;

	public NotSupportedException(String msg) {
        super(msg);
    }

    public static NotSupportedException steam() {
        return new NotSupportedException("Not yet supported for Steam");
    }

    public static NotSupportedException dungeonMode(int mode) {
        return new NotSupportedException(String.format("Not supported mode = %d, valid values are: 1 (NORMAL), 2 (HARD) or 3 (HEROIC)"));
    }
}
