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

    public static boolean areColorsSimilar(DynamicRgb dRgb1, int rgb2, int tolerance) {
        if (isMatch(getRed(rgb2), dRgb1.red, tolerance)
                && isMatch(getGreen(rgb2), dRgb1.green, tolerance)
                && isMatch(getBlue(rgb2), dRgb1.blue, tolerance))
            return true;
        if (!dRgb1.isSame)
            return false;
        DynamicRgb dRgb2 = new DynamicRgb(rgb2, dRgb1.acceptedTolerant);
        if (!dRgb2.isSame)
            return false;
        return isMatch(dRgb2.red, dRgb1.red, dRgb1.acceptedTolerant);
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

    public static class DynamicRgb {
        public final int red;
        public final int green;
        public final int blue;
        public final boolean isSame;
        public final int acceptedTolerant;

        public DynamicRgb(int rgb, int acceptedTolerant) {
            this.red = (rgb >> 16) & 0xFF;
            this.green = (rgb >> 8) & 0xFF;
            this.blue = rgb & 0xFF;
            this.isSame = this.red == this.blue && this.red == this.green;
            this.acceptedTolerant = acceptedTolerant;
        }
    }
}