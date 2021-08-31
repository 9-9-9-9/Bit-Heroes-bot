package bh.bot.app;

import bh.bot.common.types.annotations.AppCode;

@AppCode(code = "rerun")
public class SettingApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {

    }

    @Override
    protected String getAppName() {
        return "Setting";
    }

    @Override
    protected String getScriptFileName() {
        return "setting";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Do setting raid level, raid mode,...";
    }

    @Override
    protected String getLimitationExplain() {
        return "This is an utility for setting purpose only";
    }

    @Override
    protected boolean isSupportSteamScreenResolution() {
        return true;
    }

    @Override
    protected boolean isRequiredToLoadImages() {
        return false;
    }
}
