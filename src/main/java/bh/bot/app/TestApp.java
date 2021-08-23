package bh.bot.app;

import java.awt.*;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        Point point = detectLabelFishing();
        if (point == null) {
            err("Not found");
            return;
        }

        info("%d, %d", point.x, point.y);
    }

    @Override
    public String getAppCode() {
        return "test";
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
    protected String getFlags() {
        return null;
    }
}
