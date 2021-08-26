package bh.bot.app;

public class WorldBoss extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {

    }

    @Override
    public String getAppCode() {
        return "world-boss";
    }

    @Override
    protected String getAppName() {
        return "BH-World Boss";
    }

    @Override
    protected String getScriptFileName() {
        return "world-boss";
    }

    @Override
    protected String getUsage() {
        return "";
    }

    @Override
    protected String getDescription() {
        return null;
    }

    @Override
    protected String getFlags() {
        return null;
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
