package bh.bot.common.types.images;

import java.awt.image.BufferedImage;

public class BufferedImageInfo {
    public final BufferedImage bufferedImage;
    public final String code;

    public BufferedImageInfo(BufferedImage bufferedImage, String fileName) {
        this.bufferedImage = bufferedImage;
        String normalized = fileName.replace("/", "_");
        if (normalized.startsWith("_"))
            normalized = normalized.substring(1);
        if (normalized.endsWith(".bmp"))
            normalized = normalized.substring(0, normalized.length() - 4);
        this.code = normalized;
    }
}
