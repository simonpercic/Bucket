package com.github.simonpercic.bucket.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class StringUtilsTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testStringNull() throws Exception {
        boolean empty = StringUtils.isEmpty(null);
        assertTrue(empty);
    }

    @Test
    public void testStringEmpty() throws Exception {
        boolean empty = StringUtils.isEmpty("");
        assertTrue(empty);
    }

    @Test
    public void testStringNotEmpty() throws Exception {
        boolean empty = StringUtils.isEmpty("test");
        assertFalse(empty);
    }
}
