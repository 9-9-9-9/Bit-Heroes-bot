package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.utils.InteractionUtil;

import java.awt.image.BufferedImage;

import static bh.bot.common.utils.ImageUtil.freeMem;

@AppCode(code = "sc")
public class ScreenCaptureApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        int x = Configuration.gameScreenOffset.X;
        int y = Configuration.gameScreenOffset.Y;
        int w = 800;
        int h = Configuration.isSteamProfile
                ? 480
                : 520;

        BufferedImage sc = InteractionUtil.Screen.captureScreen(x, y, w, h);
        try {
            saveImage(sc, "screen-shot");
        } finally {
            freeMem(sc);
        }
    }

    @Override
    protected String getAppName() {
        return "Screen Shot";
    }

    @Override
    protected String getScriptFileName() {
        return "sc";
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "Screenshot at specific rectangle based on `offset.screen.x & y` flags, size based on profile --web 800x520 or --steam 800x480";
    }

    @Override
    protected String getLimitationExplain() {
        return "developers only";
    }

    @Override
    protected boolean isRequiredToLoadImages() {
        return false;
    }
}
