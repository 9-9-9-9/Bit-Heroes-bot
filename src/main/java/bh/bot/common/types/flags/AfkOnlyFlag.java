package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;

/**
 * Base class for the parameter-less flags that toggle an AFK-mode activity.
 *
 * <p>Historically each of these flags was its own ~25-line class that only
 * differed by keyword and description. They all share the exact same
 * behaviour: they are local (not global) flags supported solely by
 * {@link AfkApp}. Concrete flags now supply just their keyword and
 * description via the constructor.
 *
 * <p>Each activity intentionally remains a distinct subclass so the existing
 * {@code instanceof FlagDoXxx} dispatch in {@code Main} continues to work.
 */
public abstract class AfkOnlyFlag extends FlagPattern.NonParamFlag {
    private final String name;
    private final String description;

    protected AfkOnlyFlag(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final boolean isGlobalFlag() {
        return false;
    }

    @Override
    protected final boolean internalCheckIsSupportedByApp(AbstractApplication instance) {
        return instance instanceof AfkApp;
    }
}
