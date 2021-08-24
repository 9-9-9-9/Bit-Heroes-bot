package bh.bot.app;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.awt.*;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

public class TestApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        CMath lib = Native.load(Platform.isWindows()?"msvcrt":"c", CMath.class);
        double result = lib.cosh(0);
        info(result);
    }

    public interface CMath extends Library {
        double cosh(double value);
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
