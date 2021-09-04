package bh.bot.common.exceptions;

public class NotImplementedException extends RuntimeException {
	private static final long serialVersionUID = -9204175175045334856L;

	public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
