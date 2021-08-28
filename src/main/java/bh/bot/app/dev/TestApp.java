package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.types.annotations.AppCode;

@AppCode(code = "test")
public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
    }

    @Override
    protected String getAppName() {
        return "BH-Test code";
    }

    @Override
    protected String getScriptFileName() {
        return "test";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "(developers only) run test code";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
