package ru.shishmakov;

import org.junit.Test;
import ru.shishmakov.util.StringUtil;

import static org.junit.Assert.assertEquals;

/**
 * Check class {@link StringUtil}
 *
 * @author Dmitriy Shishmakov
 */
public class StringUtilTest extends TestBase {
    private static final String length1024 = "Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024";
    private static final String length25 = "Text 25. Text 25. Text 25";
    private static final String empty = "";
    private static final String nullString = null;

    @Test
    public void substringShouldTruncateAndAddEllipsisIfLengthIsNotEnough() {
        assertEquals("Text length 1024. Text le ...", StringUtil.substringEllipsis(length1024, 0, 25));
        assertEquals("Text 25. Text 25. Text 2 ...", StringUtil.substringEllipsis(length25, 0, length25.length() - 1));
    }

    @Test
    public void substringShouldNotTruncateIfLengthIsEnough() {
        assertEquals(length25, StringUtil.substringEllipsis(length25, 0, length25.length() + 1));
        assertEquals(length25, StringUtil.substringEllipsis(length25, 0, length25.length()));
        assertEquals("", StringUtil.substringEllipsis(empty, 0, 25));
        assertEquals(null, StringUtil.substringEllipsis(nullString, 0, 25));
    }
}
