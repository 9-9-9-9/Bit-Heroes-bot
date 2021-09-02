package bh.bot.common.exceptions;

public class InvalidDataException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4507334783879699636L;

	public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String format, Object...params) {
        this(String.format(format, params));
    }
}
