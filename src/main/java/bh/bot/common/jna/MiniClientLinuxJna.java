package bh.bot.common.jna;

import java.awt.Rectangle;

import com.sun.jna.platform.win32.WinDef.HWND;

import bh.bot.common.exceptions.NotImplementedException;

public class MiniClientLinuxJna extends AbstractLinuxJna {
	@Override
	public Rectangle getRectangle(HWND hwnd) {
		throw new NotImplementedException("MiniClientLinuxJna::getRectangle");
	}

}