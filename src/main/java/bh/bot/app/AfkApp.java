package bh.bot.app;

public class AfkApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {

    }

    @Override
    public String getAppCode() {
        return "afk";
    }

    @Override
    protected String getAppName() {
        return "BH-AFK";
    }

    @Override
    protected String getScriptFileName() {
        return "afk";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Burns your turns while you are AFK";
    }

    @Override
    protected String getFlags() {
        return buildFlags(
                "--invasion : do Invasion",
                "--trials : do Trials",
                "--pvp : do PVP",
                "--boss : do World Boss",
                "--raid : do Raid",
                "--exit=X : exit after X seconds if turns not all consumed"
        );
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
