package bh.bot.common.exceptions;

public class NotSupportedException extends RuntimeException {
	private static final long serialVersionUID = 8411478604990797549L;

	public NotSupportedException(String msg) {
        super(msg);
    }

    public static NotSupportedException steam() {
        return new NotSupportedException("Not yet supported for Steam");
    }
}
