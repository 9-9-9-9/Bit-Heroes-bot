package bh.bot.common.utils;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the colour primitives that underpin the bot's "vision".
 * These methods are pure functions, so they can be locked down without a
 * running game window - exactly the kind of safety net that makes the
 * matching engine safe to refactor.
 */
class ImageUtilTest {
    private static int rgb(int r, int g, int b) {
        return new Color(r, g, b).getRGB();
    }

    @Test
    void channelExtraction_returnsIndividualBytes() {
        int color = rgb(0x12, 0x34, 0x56);
        assertEquals(0x12, ImageUtil.getRed(color));
        assertEquals(0x34, ImageUtil.getGreen(color));
        assertEquals(0x56, ImageUtil.getBlue(color));
    }

    @Test
    void channelExtraction_handlesBoundaryValues() {
        assertEquals(255, ImageUtil.getRed(rgb(255, 0, 0)));
        assertEquals(255, ImageUtil.getGreen(rgb(0, 255, 0)));
        assertEquals(255, ImageUtil.getBlue(rgb(0, 0, 255)));
        assertEquals(0, ImageUtil.getRed(rgb(0, 128, 128)));
    }

    @Test
    void areColorsSimilar_identicalColorsAlwaysMatch() {
        int c = rgb(100, 150, 200);
        assertTrue(ImageUtil.areColorsSimilar(c, c, 0));
    }

    @Test
    void areColorsSimilar_withinToleranceOnEveryChannelMatches() {
        int a = rgb(100, 150, 200);
        int b = rgb(105, 145, 210);
        assertTrue(ImageUtil.areColorsSimilar(a, b, 10));
        assertFalse(ImageUtil.areColorsSimilar(a, b, 5)); // blue differs by 10 > 5
    }

    @Test
    void areColorsSimilar_anyChannelOutsideToleranceFails() {
        int a = rgb(0, 0, 0);
        int b = rgb(0, 0, 50);
        assertFalse(ImageUtil.areColorsSimilar(a, b, 49));
        assertTrue(ImageUtil.areColorsSimilar(a, b, 50));
    }

    @Test
    void isRedLikeColor_classifiesStrongRed() {
        assertTrue(ImageUtil.isRedLikeColor(new Color(220, 20, 20)));
        assertFalse(ImageUtil.isRedLikeColor(new Color(220, 200, 20)));
        assertFalse(ImageUtil.isRedLikeColor(new Color(150, 20, 20)));
    }

    @Test
    void isGreenLikeColor_classifiesStrongGreen() {
        assertTrue(ImageUtil.isGreenLikeColor(new Color(20, 220, 20)));
        assertFalse(ImageUtil.isGreenLikeColor(new Color(220, 220, 20)));
        assertFalse(ImageUtil.isGreenLikeColor(new Color(20, 150, 20)));
    }
}
