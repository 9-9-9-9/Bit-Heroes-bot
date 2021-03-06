package bh.bot.common.utils;

import bh.bot.common.Configuration;
import bh.bot.common.types.Offset;
import bh.bot.common.types.images.BufferedImageInfo;
import bh.bot.common.types.tuples.Tuple2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static bh.bot.common.Log.err;

public class ImageUtil {
    public static BufferedImageInfo loadMxImageFromResource(String path) throws IOException {
        if (!path.toLowerCase().trim().endsWith("-mx.bmp"))
            throw new IllegalArgumentException("Only accept *-mx.bmp pictures");
        return loadImageFromResource(path);
    }

    public static BufferedImageInfo loadTpImageFromResource(String path) throws IOException {
        if (!path.toLowerCase().trim().endsWith("-tp.bmp"))
            throw new IllegalArgumentException("Only accept *-tp.bmp pictures");
        return loadImageFromResource(path);
    }

    public static BufferedImageInfo loadImageFromResource(String path) throws IOException {
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

    public static Tuple2<BufferedImageInfo, Offset> transformFromTpToMxImage(BufferedImageInfo bii, int blackPixelRgb, Offset originalOffset) {
        if (bii.notAvailable)
            return new Tuple2<>(bii, originalOffset);

        int tolerant = Configuration.Tolerant.colorBw;
        TestTransformMxResult testTransformMxResult = testTransformMx(bii.bufferedImage, blackPixelRgb, tolerant);
        freeMem(testTransformMxResult.tp);
        return new Tuple2<>(new BufferedImageInfo(testTransformMxResult.mx, bii.code), testTransformMxResult.mxOffset);
    }

    public static TestTransformMxResult testTransformMx(BufferedImage bi, int rgb, int tolerant) {
        BufferedImage mx;
        BufferedImage tp;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        final ImageUtil.DynamicRgb dRgb = new ImageUtil.DynamicRgb(rgb, tolerant);

        java.util.List<int[]> pos = new ArrayList<>();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int posRgb = bi.getRGB(x, y) & 0xFFFFFF;
                if (!ImageUtil.areColorsSimilar(dRgb, posRgb, Configuration.Tolerant.color, null)) {
                    continue;
                }
                pos.add(new int[]{x, y});

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        List<int[]> translatedPos = new ArrayList<>();
        for (int[] p : pos)
            translatedPos.add(new int[]{p[0] - minX, p[1] - minY});

        final int white = 0xFFFFFF;
        final int black = 0x000000;
        final int red = 0xFF0000;

        BufferedImage biSample = new BufferedImage(maxX - minX + 1, maxY - minY + 1, 5);
        try {
            for (int[] p : translatedPos)
                biSample.setRGB(p[0], p[1], white);

            mx = new BufferedImage(biSample.getWidth(), biSample.getHeight(), biSample.getType());
            for (int y = 0; y < mx.getHeight(); y++) {
                for (int x = 0; x < mx.getWidth(); x++) {
                    mx.setRGB(x, y, white);
                }
            }

            for (int y = 0; y < biSample.getHeight(); y++) {
                for (int x = 0; x < biSample.getWidth(); x++) {
                    int rgbFromSample = biSample.getRGB(x, y) & 0xFFFFFF;
                    if (rgbFromSample != white)
                        continue;

                    mx.setRGB(x, y, black);

                    // surround with red
                    for (int oY = -1; oY <= 1; oY++) {
                        for (int oX = -1; oX <= 1; oX++) {
                            if (oX == 0 && oY == 0)
                                continue;
                            int sX = x + oX;
                            int sY = y + oY;
                            if (sX < 0 || sX >= biSample.getWidth())
                                continue;
                            if (sY < 0 || sY >= biSample.getHeight())
                                continue;
                            rgbFromSample = biSample.getRGB(sX, sY) & 0xFFFFFF;
                            if (rgbFromSample == black)
                                mx.setRGB(sX, sY, red);
                        }
                    }
                }
            }

            tp = new BufferedImage(mx.getWidth(), mx.getHeight(), mx.getType());
            for (int y = 0; y < tp.getHeight(); y++)
                for (int x = 0; x < tp.getWidth(); x++)
                	try {
                        tp.setRGB(x, y, bi.getRGB(x + minX, y + minY));
                	} catch (ArrayIndexOutOfBoundsException ex) {
                		err("Problem while moving pixel from bi %d,%d to tp %d,%d (minX = %d, minY = %d)", x, y, x + minX, y + minY, minX, minY);
                		throw ex;
                	}

            return new TestTransformMxResult(bi, mx, tp, new Offset(minX, minY));
        } finally {
            freeMem(biSample);
        }
    }

    public static boolean areColorsSimilar(int rgb1, int rgb2, int tolerance) {
        return isMatch(getRed(rgb1), getRed(rgb2), tolerance)
                && isMatch(getGreen(rgb1), getGreen(rgb2), tolerance)
                && isMatch(getBlue(rgb1), getBlue(rgb2), tolerance);
    }

    public static boolean areColorsSimilar(DynamicRgb dRgb1, int rgb2, int tolerance, Short originalPixelPart) {
        if (isMatch(getRed(rgb2), dRgb1.red, tolerance)
                && isMatch(getGreen(rgb2), dRgb1.green, tolerance)
                && isMatch(getBlue(rgb2), dRgb1.blue, tolerance))
            return true;
        if (!dRgb1.isSame)
            return false;
        DynamicRgb dRgb2 = new DynamicRgb(rgb2, dRgb1.acceptedTolerant);
        if (!dRgb2.isSame)
            return false;
        if (isMatch(dRgb2.red, dRgb1.red, dRgb1.acceptedTolerant))
            return true;
        if (originalPixelPart == null)
            return false;
        return isMatch(originalPixelPart, getRed(rgb2), Configuration.Tolerant.colorBwL2);
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

    public static void freeMem(BufferedImage bi) {
        if (bi == null)
            return;
        try {
            bi.flush();
        } catch (Exception ignored) {
            //
        }
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

    public static class TestTransformMxResult {
        public final BufferedImage original;
        public final BufferedImage mx;
        public final BufferedImage tp;
        public final Offset mxOffset;

        public TestTransformMxResult(BufferedImage original, BufferedImage mx, BufferedImage tp, Offset mxOffset) {
            this.original = original;
            this.mx = mx;
            this.tp = tp;
            this.mxOffset = mxOffset;
        }
    }
}