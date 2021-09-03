package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.linux.Udev;
import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.Configuration.Offset;
import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple4;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class MiniClientLinuxJna extends AbstractLinuxJna {
	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientLinuxJna::getRectangle");
	}

}
