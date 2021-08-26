package bh.bot.common.exceptions;

public class InvalidFlagException extends Exception {
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
