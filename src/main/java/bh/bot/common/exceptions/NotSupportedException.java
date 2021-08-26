package bh.bot.common.exceptions;

public class NotSupportedException extends RuntimeException {
    public NotSupportedException(String msg) {
        super(msg);
    }

    public static NotSupportedException steam() {
        return new NotSupportedException("Not yet supported for Steam");
    }
}
