package bh.bot.common.utils;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-image test harness for the bot's pattern-matching ("vision").
 *
 * <p>The production matcher ({@link bh.bot.common.types.images.BwMatrixMeta})
 * recognises a UI element by remembering two sets of pixel coordinates:
 * <ul>
 *   <li><b>match pixels</b> - must be close to a target colour, and</li>
 *   <li><b>anti-match pixels</b> - must NOT be close to that colour.</li>
 * </ul>
 * It then scans a screenshot for the offset where every match pixel matches
 * and every anti-match pixel does not. {@link Pattern} below reproduces that
 * algorithm in isolation (using the same {@link ImageUtil#areColorsSimilar}
 * primitive) so a pattern can be asserted against a fixed image without a
 * running game.
 *
 * <p>The fixtures here are generated deterministically in-memory so the suite
 * is hermetic and CI-friendly. To pin behaviour against real game art, drop a
 * screenshot into {@code src/test/resources/golden/} and build a {@link Pattern}
 * from the known pixel coordinates of the element you expect to find.
 */
class GoldenImageMatchTest {

    /** Tolerance roughly matching the bot's default colour tolerance. */
    private static final int TOLERANCE = 12;

    @Test
    void findsPatternAtItsExactLocation() {
        BufferedImage scene = solid(64, 48, Color.WHITE);
        // A small black glyph at (10,8)-(12,8).
        paint(scene, 10, 8, Color.BLACK);
        paint(scene, 11, 8, Color.BLACK);
        paint(scene, 12, 8, Color.BLACK);

        Pattern glyph = new Pattern(Color.BLACK)
                .match(0, 0).match(1, 0).match(2, 0)
                .antiMatch(0, 1); // pixel below the glyph stays white

        int[] hit = glyph.findIn(scene, TOLERANCE);
        assertNotNull(hit, "pattern should be located in the scene");
        assertArrayEquals(new int[]{10, 8}, hit);
    }

    @Test
    void returnsNullWhenPatternAbsent() {
        BufferedImage emptyScene = solid(64, 48, Color.WHITE);
        Pattern glyph = new Pattern(Color.BLACK).match(0, 0).match(1, 0).match(2, 0);
        assertNull(glyph.findIn(emptyScene, TOLERANCE));
    }

    @Test
    void antiMatchPixelRejectsAFalsePositive() {
        // A solid 3x2 black block at (10,8)-(12,9). At the block's top-left,
        // a pattern that requires the pixel below the top row to be non-black
        // must reject the location (that pixel is black). A lenient pattern
        // without the anti-match constraint accepts it.
        BufferedImage scene = solid(64, 48, Color.WHITE);
        for (int x = 10; x <= 12; x++)
            for (int y = 8; y <= 9; y++)
                paint(scene, x, y, Color.BLACK);

        Pattern strict = new Pattern(Color.BLACK)
                .match(0, 0).match(1, 0).match(2, 0)
                .antiMatch(0, 1); // pixel below the top row must be non-black
        assertFalse(strict.matchesAt(scene, 10, 8, TOLERANCE),
                "anti-match pixel should veto the block's top-left");

        Pattern lenient = new Pattern(Color.BLACK).match(0, 0).match(1, 0).match(2, 0);
        assertTrue(lenient.matchesAt(scene, 10, 8, TOLERANCE));
    }

    @Test
    void matchHonoursColourTolerance() {
        BufferedImage scene = solid(32, 32, Color.WHITE);
        // Near-black, 10 per channel away from pure black.
        paint(scene, 5, 5, new Color(10, 10, 10));

        Pattern glyph = new Pattern(Color.BLACK).match(0, 0);
        assertNotNull(glyph.findIn(scene, 12), "within tolerance should match");
        assertNull(glyph.findIn(scene, 5), "outside tolerance should not match");
    }

    @Test
    void matchesAt_isPositionSpecific() {
        BufferedImage scene = solid(32, 32, Color.WHITE);
        paint(scene, 7, 7, Color.BLACK);
        Pattern glyph = new Pattern(Color.BLACK).match(0, 0);
        assertTrue(glyph.matchesAt(scene, 7, 7, TOLERANCE));
        assertFalse(glyph.matchesAt(scene, 8, 7, TOLERANCE));
    }

    @Test
    void scanCountsAllOccurrences() {
        BufferedImage scene = solid(32, 32, Color.WHITE);
        paint(scene, 3, 3, Color.BLACK);
        paint(scene, 20, 12, Color.BLACK);
        Pattern dot = new Pattern(Color.BLACK).match(0, 0);
        assertEquals(2, dot.countIn(scene, TOLERANCE));
    }

    // ---- helpers -------------------------------------------------------

    private static BufferedImage solid(int w, int h, Color c) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int rgb = c.getRGB();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, rgb);
        return bi;
    }

    private static void paint(BufferedImage bi, int x, int y, Color c) {
        bi.setRGB(x, y, c.getRGB());
    }

    /**
     * Self-contained re-implementation of the bot's match / anti-match pixel
     * scan, exposed for tests. Coordinates are relative to the pattern's
     * top-left origin.
     */
    static final class Pattern {
        private final int targetRgb;
        private final List<int[]> matchPixels = new ArrayList<>();
        private final List<int[]> antiMatchPixels = new ArrayList<>();

        Pattern(Color target) {
            this.targetRgb = target.getRGB();
        }

        Pattern match(int dx, int dy) {
            matchPixels.add(new int[]{dx, dy});
            return this;
        }

        Pattern antiMatch(int dx, int dy) {
            antiMatchPixels.add(new int[]{dx, dy});
            return this;
        }

        boolean matchesAt(BufferedImage img, int ox, int oy, int tolerance) {
            for (int[] p : matchPixels) {
                if (!inBounds(img, ox + p[0], oy + p[1]))
                    return false;
                if (!ImageUtil.areColorsSimilar(targetRgb, img.getRGB(ox + p[0], oy + p[1]), tolerance))
                    return false;
            }
            for (int[] p : antiMatchPixels) {
                if (!inBounds(img, ox + p[0], oy + p[1]))
                    return false;
                if (ImageUtil.areColorsSimilar(targetRgb, img.getRGB(ox + p[0], oy + p[1]), tolerance))
                    return false;
            }
            return true;
        }

        /** @return the {x,y} of the first (top-left-most) match, or null. */
        int[] findIn(BufferedImage img, int tolerance) {
            for (int y = 0; y < img.getHeight(); y++)
                for (int x = 0; x < img.getWidth(); x++)
                    if (matchesAt(img, x, y, tolerance))
                        return new int[]{x, y};
            return null;
        }

        int countIn(BufferedImage img, int tolerance) {
            int count = 0;
            for (int y = 0; y < img.getHeight(); y++)
                for (int x = 0; x < img.getWidth(); x++)
                    if (matchesAt(img, x, y, tolerance))
                        count++;
            return count;
        }

        private static boolean inBounds(BufferedImage img, int x, int y) {
            return x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight();
        }
    }
}
