package bh.bot.common.types.images;

import bh.bot.common.Configuration;
import bh.bot.common.utils.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class BwMatrixMeta {
    private final ArrayList<int[]> blackPixels;
    private final ArrayList<int[]> nonBlackPixels;
    private final int w;
    private final int h;
    private final int blackPixelRgb;
    private final Color colorBlackPixel;
    private final Configuration.Offset coordinateOffset;
    private int[] lastMatch = new int[]{-1, -1};

    public BwMatrixMeta(BufferedImage img, Configuration.Offset coordinateOffset, int blackPixelRgb) {
        final int anyColorPixelRgb = 0xFFFFFF;
        try {
            this.coordinateOffset = coordinateOffset;
            this.blackPixelRgb = blackPixelRgb & 0xFFFFFF;
            this.colorBlackPixel = new Color(this.blackPixelRgb);
            blackPixels = new ArrayList<>();
            nonBlackPixels = new ArrayList<>();
            w = img.getWidth();
            h = img.getHeight();
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int rgb = img.getRGB(x, y) & 0xFFFFFF;
                    if (rgb == blackPixelRgb)
                        blackPixels.add(new int[]{x, y});
                    else if (rgb != anyColorPixelRgb)
                        nonBlackPixels.add(new int[]{x, y});
                }
            }
        } finally {
            img.flush();
        }
    }

    public boolean isMatchBlackRgb(int rgb) {
        return ImageUtil.areColorsSimilar(colorBlackPixel, new Color(rgb), Configuration.Tolerant.color);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getBlackPixelRgb() {
        return blackPixelRgb;
    }

    public ArrayList<int[]> getBlackPixels() {
        return blackPixels;
    }

    public ArrayList<int[]> getNonBlackPixels() {
        return nonBlackPixels;
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

    public static class Metas {
        public static class Globally {
            public static class Buttons {
                public static BwMatrixMeta talkRightArrow;
            }
        }

        public static class Fishing {
            public static class Labels {
                public static BwMatrixMeta fishing;
            }
        }
    }

    public static void load() throws IOException {
        BwMatrixMeta.Metas.Globally.Buttons.talkRightArrow = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/talkArrow-mx.bmp"
                ), //
                Configuration.Offsets.Globally.Buttons.talkRightArrow,
                0x000000
        );
        BwMatrixMeta.Metas.Fishing.Labels.fishing = new BwMatrixMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "labels/fishing-mx2.bmp"
                ), //
                Configuration.Offsets.Fishing.Labels.fishing,
                0x000000
        );
    }
}