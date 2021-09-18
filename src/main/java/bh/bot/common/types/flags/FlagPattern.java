package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.common.OS;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class FlagPattern<T> {
    private final ArrayList<String> rawFlags = new ArrayList<>();

    public final void pushRaw(String raw) {
        rawFlags.add(raw);
    }

    public final ArrayList<T> parseParams() throws InvalidFlagException {
        ArrayList<T> result = new ArrayList<>();
        for (String rawFlag : rawFlags) {
            result.add(parseParam(rawFlag));
        }
        return result;
    }

    public final T parseParam(String raw) throws InvalidFlagException {
        if (!isAllowParam())
            throw new NotSupportedException(String.format("Flag '%s' does not support parameter", getCode()));

        if (isAllowEmptyParam() && !raw.contains("=")) {
        	if (!raw.equals(getCode()))
                throw new InvalidFlagException("Invalid flag header (passing wrong flag?)");
        	return getDefaultValueWhenEmptyParam();
        }
        
        if (!raw.contains("="))
            throw new InvalidFlagException(String.format("Flag '%s=?' expected parameter but doesn't contains parameter", getCode()));

        if (!raw.startsWith(String.format("%s=", getCode())))
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

    public abstract boolean isGlobalFlag();

    public final boolean isSupportedByApp(AbstractApplication instance) {
        if (isGlobalFlag())
            return true;
        return internalCheckIsSupportedByApp(instance);
    }

    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        throw new NotImplementedException();
    }

    private int countMatch = 0;
    public final boolean isThisFlag(String raw) throws InvalidFlagException {
        String prefix = String.format("--%s", getName());
        if (raw.equals(prefix)) {
            if (!isAllowParam() || !isAllowEmptyParam())
                throw new InvalidFlagException(String.format("Flag '%s' is invalid, must contains parameter or have to allow empty param", raw));
            countMatch++;
            if (countMatch > 1 && !isAllowMultiple())
                throw new NotSupportedException(String.format("Flag '--%s' can not be declared multiple times", getName()));
            return true;
        }
        if (raw.startsWith(prefix + "=")) {
            if (!isAllowParam())
                throw new InvalidFlagException(String.format("Flag '--%s' does not contains parameter", getName()));
            countMatch++;
            if (countMatch > 1 && !isAllowMultiple())
                throw new NotSupportedException(String.format("Flag '--%s' can not be declared multiple times", getName()));
            return true;
        }
        return false;
    }

    public boolean isAllowParam() {
        return false;
    }
    
    public boolean isAllowEmptyParam() {
        return false;
    }
    
    public T getDefaultValueWhenEmptyParam() {
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isAllowMultiple() {
        return false;
    }

    public boolean isDevelopersOnly() {
        return false;
    }

    public abstract String getName();
    public abstract String getDescription();
    public final boolean isSupportedOnCurrentOsPlatform() {
        return Arrays.asList(getSupportedOsPlatforms()).contains(OS.platform);
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Linux, Platform.Windows, Platform.MacOS };
    }

    private final String code = "--" + getName();
    public String getCode() {
        return code;
    }

    @Override
    public final String toString() {
        String text = String.format("\n  %s%s : %s%s", getCode(), isAllowParam() ? "=?" : "", isDevelopersOnly() ? "(developers only) " : "", getDescription());
        if (!isSupportedOnCurrentOsPlatform())
            text += ". Only available on " + Arrays.stream(getSupportedOsPlatforms()).map(Enum::toString).collect(Collectors.joining(", ")) + " OS";
        return text;
    }

    public static abstract class NonParamFlag extends FlagPattern<Void> {
    }
}
