package bh.bot.common.types.images;

import java.awt.image.BufferedImage;

public class BufferedImageInfo {
    public final BufferedImage bufferedImage;
    public final String code;
    public final boolean notAvailable;

    private BufferedImageInfo(BufferedImage bufferedImage, String fileName, boolean notAvailable) {
        this.bufferedImage = bufferedImage;
        String normalized = fileName.replace("/", "_");
        if (normalized.startsWith("_"))
            normalized = normalized.substring(1);
        if (normalized.endsWith(".bmp"))
            normalized = normalized.substring(0, normalized.length() - 4);
        this.code = normalized;
        this.notAvailable = notAvailable;
    }

    public BufferedImageInfo(BufferedImage bufferedImage, String fileName) {
        this(bufferedImage, fileName, false);
    }

    public static BufferedImageInfo notAvailable(String fileName) {
        return new BufferedImageInfo(null, fileName, true);
    }
}
