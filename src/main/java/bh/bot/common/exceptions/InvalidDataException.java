package bh.bot.common.exceptions;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String format, Object...params) {
        this(String.format(format, params));
    }
}
