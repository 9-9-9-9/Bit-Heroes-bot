package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.exceptions.NotSupportedException;
import bh.bot.common.types.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class FlagPattern<T> {
    private final ArrayList<String> rawFlags = new ArrayList<>();

    public void pushRaw(String raw) {
        rawFlags.add(raw);
    }

    public ArrayList<T> parseParams() throws InvalidFlagException {
        ArrayList<T> result = new ArrayList<>();
        for (String rawFlag : rawFlags) {
            result.add(parseParam(rawFlag));
        }
        return result;
    }

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

    public boolean isSupportedByApp(AbstractApplication instance) {
        if (isGlobalFlag())
            return true;
        return internalCheckIsSupportedByApp(instance);
    }

    protected boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        throw new NotImplementedException();
    }

    private int countMatch = 0;
    public boolean isThisFlag(String raw) throws InvalidFlagException {
        String prefix = String.format("--%s", getName());
        if (raw.equals(prefix)) {
            if (isAllowParam())
                throw new InvalidFlagException(String.format("Flag '%s' is invalid, must contains parameter", raw));
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

    protected boolean isAllowMultiple() {
        return false;
    }

    public boolean isDevelopersOnly() {
        return false;
    }

    public abstract String getName();
    public abstract String getDescription();
    public final boolean isSupportedOnCurrentOsPlatform() {
        return Arrays.asList(getSupportedOsPlatforms()).contains(Configuration.OS.platform);
    }

    public Platform[] getSupportedOsPlatforms() {
        return new Platform[] { Platform.Linux, Platform.Windows, Platform.MacOS };
    }

    public String getCode() {
        return "--" + getName();
    }

    @Override
    public String toString() {
        String text = String.format("\n  %s%s : %s%s", getCode(), isAllowParam() ? "=?" : "", isDevelopersOnly() ? "(developers only) " : "", getDescription());
        if (!isSupportedOnCurrentOsPlatform())
            text += ". Only available on " + String.join(", ", Arrays.asList(getSupportedOsPlatforms()).stream().map(x -> x.toString()).collect(Collectors.toList())) + " OS";
        return text;
    }

    public static abstract class NonParamFlag extends FlagPattern<Void> {
    }
}
