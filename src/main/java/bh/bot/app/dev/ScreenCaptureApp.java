package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.utils.InteractionUtil;

import java.awt.image.BufferedImage;

import static bh.bot.common.utils.ImageUtil.freeMem;

@AppMeta(code = "sc", name = "Screen Shot", dev = true)
public class ScreenCaptureApp extends AbstractApplication {
	@Override
	protected void internalRun(String[] args) {
		adjustScreenOffset();
		int x = Configuration.gameScreenOffset.X.get();
		int y = Configuration.gameScreenOffset.Y.get();
		int w = Configuration.screenResolutionProfile.getSupportedGameResolutionWidth();
		int h = Configuration.screenResolutionProfile.getSupportedGameResolutionHeight();

		BufferedImage sc = InteractionUtil.Screen.captureScreen(x, y, w, h);
		try {
			saveImage(sc, "screen-shot");
		} finally {
			freeMem(sc);
		}
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
