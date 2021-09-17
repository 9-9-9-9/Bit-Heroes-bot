package bh.bot.common.utils;

import java.util.Random;

public class RandomUtil {
	private static final Random rad = new Random();
	
	public static int nextInt(int upperBound) {
		return rad.nextInt(upperBound);
	}
}
