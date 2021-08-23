package bh.bot.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageUtil {
    public static BufferedImage loadImageFileFromResource(String path) throws IOException {
        if (!path.toLowerCase().endsWith(".bmp"))
            throw new IllegalArgumentException("Only accept bmp pictures");
        BufferedImage bi = ImageIO.read(ImageUtil.class.getResource("/game-images/" + path));
        final int acceptedType = BufferedImage.TYPE_3BYTE_BGR;
        if (bi.getType() != acceptedType)
            throw new IllegalArgumentException(String.format("Only accept bmp pictures with type = BufferedImage.TYPE_3BYTE_BGR (int value = %d)", acceptedType));
        return bi;
    }

    public static boolean areColorsSimilar(Color color1, Color color2, int tolerance) {
        return isMatch(color1.getRed(), color2.getRed(), tolerance)
                && isMatch(color1.getGreen(), color2.getGreen(), tolerance)
                && isMatch(color1.getBlue(), color2.getBlue(), tolerance);
    }

    public static boolean areColorsSimilar(int rgb1, int rgb2, int tolerance) {
        return isMatch(getRed(rgb1), getRed(rgb2), tolerance)
                && isMatch(getGreen(rgb1), getGreen(rgb2), tolerance)
                && isMatch(getBlue(rgb1), getBlue(rgb2), tolerance);
    }

    private static boolean isMatch(int c1, int c2, int tolerant) {
        return Math.abs(c1 - c2) <= tolerant;
    }

    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBlue(int rgb) {
        return rgb & 0xFF;
    }
}