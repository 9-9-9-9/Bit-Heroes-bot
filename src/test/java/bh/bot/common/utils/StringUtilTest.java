package bh.bot.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilTest {
    @Test
    void isBlank_treatsNullEmptyAndWhitespaceAsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\n"));
        assertFalse(StringUtil.isBlank("x"));
        assertFalse(StringUtil.isBlank("  x  "));
    }

    @Test
    void isNotBlank_isInverseOfIsBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank("  "));
        assertTrue(StringUtil.isNotBlank("hello"));
    }

    @Test
    void isTrue_acceptsCommonTruthyTokensCaseInsensitively() {
        assertTrue(StringUtil.isTrue("true"));
        assertTrue(StringUtil.isTrue("TRUE"));
        assertTrue(StringUtil.isTrue(" Yes "));
        assertTrue(StringUtil.isTrue("y"));
        assertFalse(StringUtil.isTrue("false"));
        assertFalse(StringUtil.isTrue("1"));
        assertFalse(StringUtil.isTrue(null));
        assertFalse(StringUtil.isTrue(""));
    }

    @Test
    void firstNotBlank_returnsFirstMeaningfulValueOrNull() {
        assertEquals("b", StringUtil.firstNotBlank(null, "  ", "b", "c"));
        assertEquals("a", StringUtil.firstNotBlank("a", "b"));
        assertNull(StringUtil.firstNotBlank(null, "", "   "));
    }
}
