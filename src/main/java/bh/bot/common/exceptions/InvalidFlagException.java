package bh.bot.common.exceptions;

public class InvalidFlagException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1610483178929493309L;

	public InvalidFlagException() {
        super();
    }

    public InvalidFlagException(String message) {
        super(message);
    }

    public InvalidFlagException(String message, Exception ex) {
        super(message, ex);
    }
}
