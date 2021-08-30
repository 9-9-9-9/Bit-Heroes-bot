package bh.bot.common.utils;

import bh.bot.common.Configuration;
import bh.bot.common.types.images.BufferedImageInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageUtil {
    public static BufferedImageInfo loadImageFileFromResource(String path) throws IOException {
        if (!path.toLowerCase().trim().endsWith("-mx.bmp"))
            throw new IllegalArgumentException("Only accept *-mx.bmp pictures");
        String fileName = String.format("/game-images/%s/%s", Configuration.profileName, path);
        URL resource = ImageUtil.class.getResource(fileName);
        if (resource == null)
            return BufferedImageInfo.notAvailable(fileName);
        BufferedImage bi = ImageIO.read(resource);
        final int acceptedType = BufferedImage.TYPE_3BYTE_BGR;
        if (bi.getType() != acceptedType)
            throw new IllegalArgumentException(String.format("Only accept bmp pictures with type = BufferedImage.TYPE_3BYTE_BGR (int value = %d)", acceptedType));
        return new BufferedImageInfo(bi, fileName);
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

    public static boolean isRedLikeColor(Color color) {
        int rgb = color.getRGB();
        return getRed(rgb) >= 205 && getGreen(rgb) <= 150 && getBlue(rgb) <= 150;
    }

    public static boolean isGreenLikeColor(Color color) {
        int rgb = color.getRGB();
        return getRed(rgb) <= 150 && getGreen(rgb) >= 205 && getBlue(rgb) <= 150;
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