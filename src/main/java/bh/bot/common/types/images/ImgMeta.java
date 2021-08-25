package bh.bot.common.types.images;

import bh.bot.common.Configuration;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class ImgMeta {
    private final ArrayList<Pixel> a2;
    private final int w;
    private final int h;
    private final int firstRgb;
    private final Color firstColor;
    private final Configuration.Offset coordinateOffset;
    private int[] lastMatch = new int[]{-1, -1};

    public ImgMeta(BufferedImage img, Configuration.Offset coordinateOffset) {
        final int anyColorPixelRgb = 0xFFFFFF;
        try {
            a2 = new ArrayList<>();
            w = img.getWidth();
            h = img.getHeight();
            int prevRgb = 0;
            firstRgb = img.getRGB(0, 0) & 0xFFFFFF;
            firstColor = new Color(firstRgb);
            this.coordinateOffset = coordinateOffset;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = img.getRGB(x, y) & 0xFFFFFF;
                    if (prevRgb == rgb) {
                        continue;
                    }
                    if (rgb == anyColorPixelRgb) {
                        continue;
                    }
                    a2.add(new Pixel(x, y, rgb));
                    prevRgb = rgb;
                }
            }
        } finally {
            img.flush();
        }
    }

    public boolean isMatchFirstRgb(int rgb) {
        return ImageUtil.areColorsSimilar(firstRgb, rgb, Configuration.Tolerant.color);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public ArrayList<Pixel> getPixelList() {
        return a2;
    }

    public void setLastMatchPoint(int x, int y) {
        lastMatch = new int[]{x, y};
    }

    public int[] getLastMatchPoint() {
        return lastMatch;
    }

    public Configuration.Offset getCoordinateOffset() {
        return coordinateOffset;
    }

    public static void load() throws IOException {
    }
}
