package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Telegram;
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
			Telegram.bhTelegramBot.sendPhoto(sc, getDescription());
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
		return "Screenshot at specific rectangle based on window coordinate from auto-detect or `offset.screen.x & y` flags";
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
