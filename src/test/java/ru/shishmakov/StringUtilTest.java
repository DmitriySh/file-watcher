package ru.shishmakov;

import org.junit.Assert;
import org.junit.Test;
import ru.shishmakov.util.StringUtil;

/**
 * Check class {@link StringUtil}
 *
 * @author Dmitriy Shishmakov
 */
public class StringUtilTest extends TestBase {

    @Test
    public void testSubstringTruncation() {
        final String length1024 = "Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024. Text length 1024";
        final String length4 = "Text";
        final String length25 = "Text 25. Text 25. Text 25";
        final String length24 = "Text 24. Text 24. Text 2";
        final String length26 = "Text 26. Text 26. Text 26.";
        final String empty = "";
        final String nullString = null;

        Assert.assertEquals("Text length 1024. Text le ...", StringUtil.substring(length1024, 0, 25));
        Assert.assertEquals("Text", StringUtil.substring(length4, 0, 25));
        Assert.assertEquals(length25, StringUtil.substring(length25, 0, 25));
        Assert.assertEquals(length24, StringUtil.substring(length24, 0, 25));
        Assert.assertEquals("Text 26. Text 26. Text 26 ...", StringUtil.substring(length26, 0, 25));
        Assert.assertEquals("", StringUtil.substring(empty, 0, 25));
        Assert.assertEquals(null, StringUtil.substring(nullString, 0, 25));
    }
}
