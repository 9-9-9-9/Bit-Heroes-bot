package bh.bot.app.dev;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;
import static bh.bot.common.utils.ImageUtil.freeMem;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.Configuration.Offset;
import bh.bot.common.jna.IJna;
import bh.bot.common.jna.MiniClientWindowsJna;
import bh.bot.common.jna.SteamWindowsJna;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.InteractionUtil;

@AppCode(code = "sc")
public class ScreenCaptureApp extends AbstractApplication {
	@Override
	protected void internalRun(String[] args) {
		int x = Configuration.gameScreenOffset.X.get();
		int y = Configuration.gameScreenOffset.Y.get();
		int w = 800;
		int h = Configuration.isSteamProfile ? 480 : 520;

		IJna jna = Configuration.isSteamProfile ? new SteamWindowsJna() : new MiniClientWindowsJna();
		ScreenResolutionProfile srp = Configuration.screenResolutionProfile;
		Tuple4<Boolean, String, Rectangle, Offset> result = jna.locateGameScreenOffset(null, srp);
		if (result._1) {
			if (result._4.X != x || result._4.Y != y) {
				x = result._4.X;
				y = result._4.Y;
				info("Game's screen offset has been adjusted automatically to %d,%d", x, y);
			} else {
				debug("screen offset not change");
			}
		} else {
			err("Failure detecting screen offset: %s", result._2);
		}

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
