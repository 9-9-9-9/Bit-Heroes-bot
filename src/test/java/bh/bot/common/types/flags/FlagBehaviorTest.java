package bh.bot.common.types.flags;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.common.exceptions.InvalidFlagException;
import bh.bot.common.types.annotations.AppMeta;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks down the observable behaviour of the command-line flags. These tests
 * are intentionally written against the public contract (name, parameter
 * handling, per-app support) rather than the class hierarchy, so the
 * "simple AFK activity" flags can be refactored onto a shared base class
 * without silently changing how the CLI behaves.
 */
class FlagBehaviorTest {

    /** A non-AFK application used to verify activity flags reject other apps. */
    @AppMeta(code = "fake-test-app", name = "FakeTestApp", requireClientType = false)
    private static final class FakeApp extends AbstractApplication {
        @Override protected void internalRun(String[] args) { }
        @Override protected String getUsage() { return ""; }
        @Override protected String getDescription() { return ""; }
        @Override protected String getLimitationExplain() { return ""; }
    }

    /** The AFK activity flags: name + the keyword the user types. */
    private static FlagPattern[] afkActivityFlags() {
        return new FlagPattern[]{
                new FlagDoPvp(), new FlagDoWorldBoss(), new FlagDoRaid(),
                new FlagDoInvasion(), new FlagDoQuest(), new FlagDoExpedition(),
                new FlagDoGvG(), new FlagDoTrials(), new FlagDoGauntlet(),
                new FlagDoFishing(), new FlagExitAfkAfterIfWaitResourceGeneration()
        };
    }

    @Test
    void afkActivityFlags_areLocalNonParamAndDescribed() {
        for (FlagPattern f : afkActivityFlags()) {
            assertNotNull(f.getName(), "name");
            assertFalse(f.getName().trim().isEmpty(), "name not blank for " + f.getClass().getSimpleName());
            assertNotNull(f.getDescription(), "description for " + f.getName());
            assertFalse(f.getDescription().trim().isEmpty(), "description not blank for " + f.getName());
            assertFalse(f.isGlobalFlag(), f.getName() + " should be a local flag");
            assertFalse(f.isAllowParam(), f.getName() + " should not take a parameter");
        }
    }

    @Test
    void afkActivityFlags_supportedByAfkAppOnly() {
        AfkApp afk = new AfkApp();
        FakeApp other = new FakeApp();
        for (FlagPattern f : afkActivityFlags()) {
            assertTrue(f.isSupportedByApp(afk), f.getName() + " should support AfkApp");
            assertFalse(f.isSupportedByApp(other), f.getName() + " should not support a non-AFK app");
        }
    }

    @Test
    void isThisFlag_matchesOwnKeywordOnly() throws InvalidFlagException {
        FlagPattern pvp = new FlagDoPvp();
        assertTrue(pvp.isThisFlag("--pvp"));
        assertFalse(pvp.isThisFlag("--raid"));
        assertFalse(pvp.isThisFlag("--pvpx"));
        assertFalse(pvp.isThisFlag("pvp"));
    }

    @Test
    void isThisFlag_rejectsParameterOnNonParamFlag() {
        assertThrows(InvalidFlagException.class, () -> new FlagDoPvp().isThisFlag("--pvp=3"));
    }

    @Test
    void knownKeywords_areStable() {
        // Guards against accidental renames during refactoring.
        assertEquals("pvp", new FlagDoPvp().getName());
        assertEquals("boss", new FlagDoWorldBoss().getName());
        assertEquals("raid", new FlagDoRaid().getName());
        assertEquals("invasion", new FlagDoInvasion().getName());
        assertEquals("quest", new FlagDoQuest().getName());
        assertEquals("expedition", new FlagDoExpedition().getName());
        assertEquals("gvg", new FlagDoGvG().getName());
        assertEquals("trials", new FlagDoTrials().getName());
        assertEquals("gauntlet", new FlagDoGauntlet().getName());
        assertEquals("bait", new FlagDoFishing().getName());
        assertEquals("ear", new FlagExitAfkAfterIfWaitResourceGeneration().getName());
    }

    @Test
    void registry_hasUniqueNonBlankFlagNames() {
        Set<String> seen = new HashSet<>();
        for (FlagPattern f : Flags.allFlags) {
            String name = f.getName();
            assertNotNull(name);
            assertFalse(name.trim().isEmpty(), "blank flag name in registry");
            assertTrue(seen.add(name), "duplicate flag name in registry: " + name);
        }
    }
}
