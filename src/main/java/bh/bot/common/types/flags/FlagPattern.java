package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.exceptions.NotSupportedException;

public abstract class FlagPattern<T> {
    public T parseParam(String raw) throws InvalidFlagException {
        if (!isAllowParam())
            throw new NotSupportedException(String.format("Flag '--%s' does not support parameter", getName()));

        if (!raw.contains("="))
            throw new InvalidFlagException(String.format("Flag '--%s=?' expected parameter but doesn't contains parameter"));

        if (!raw.startsWith(String.format("--%s=", getName())))
            throw new InvalidFlagException("Invalid flag header (passing wrong flag?)");

        String paramPart = raw.split("=", 2)[1];

        try {
            return internalParseParam(paramPart);
        } catch (Exception ex) {
            if (ex instanceof InvalidFlagException)
                throw ex;
            throw new InvalidFlagException(String.format("Unable to parsing parameter of flag '--%s' with reason: %s", getName(), ex.getMessage()), ex);
        }
    }

    protected T internalParseParam(String paramPart) throws InvalidFlagException {
        throw new NotImplementedException();
    }

    public boolean isGlobalFlag() {
        return true;
    }

    public <TApp extends AbstractApplication> boolean isSupportedByApp(TApp instance) {
        if (isGlobalFlag())
            return true;
        return internalCheckIsSupportedByApp(instance);
    }

    protected <TApp extends AbstractApplication> boolean internalCheckIsSupportedByApp(TApp instance) {
        throw new NotImplementedException();
    }

    public boolean isThisFlag(String raw) {
        String prefix = String.format("--%s", getName());
        return raw.equals(prefix) || raw.startsWith(prefix + "=");
    }

    public boolean isAllowParam() {
        return false;
    }

    public boolean isAllowMultiple() {
        return false;
    }

    public boolean isDevelopersOnly() {
        return false;
    }

    public abstract String getName();
    public abstract String getDescription();

    public static abstract class NonParamFlag extends FlagPattern<Void> {
    }
}
