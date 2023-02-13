package bh.bot.common.exceptions;

public class FailedQuestFindException extends RuntimeException {
	private static final long serialVersionUID = 4507334783879699636L;

	public FailedQuestFindException(String message) {
        super(message);
    }

    public FailedQuestFindException(String format, Object...params) {
        this(String.format(format, params));
    }
}
