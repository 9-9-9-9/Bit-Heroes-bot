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

    public static class Metas {

        public static class Fishing {
            public static class Buttons {
                public static ImgMeta start;
                public static ImgMeta cast;
                public static ImgMeta catch_;
            }
        }

        public static class Dungeons {
            public static class Buttons {
                public static ImgMeta rerun;
            }
        }

        public static class Globally {
            public static class Buttons {
                public static ImgMeta reconnect;
            }
        }
    }

    public static void load() throws IOException {
        Metas.Dungeons.Buttons.rerun = new ImgMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/rerun-kp.bmp"
                ), //
                Configuration.Offsets.Dungeons.Buttons.reRun);
        Metas.Globally.Buttons.reconnect = new ImgMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/reconnect-sp.bmp"
                ), //
                Configuration.Offsets.Globally.Buttons.reconnectSp);
        Metas.Fishing.Buttons.start = new ImgMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/start-sp.bmp"
                ), //
                Configuration.Offsets.Fishing.Buttons.start);
        Metas.Fishing.Buttons.cast = new ImgMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/cast-sp.bmp"
                ), //
                Configuration.Offsets.Fishing.Buttons.cast);
        Metas.Fishing.Buttons.catch_ = new ImgMeta(//
                ImageUtil.loadImageFileFromResource( //
                        "buttons/catch-sp.bmp"
                ), //
                Configuration.Offsets.Fishing.Buttons.catch_);
    }
}
