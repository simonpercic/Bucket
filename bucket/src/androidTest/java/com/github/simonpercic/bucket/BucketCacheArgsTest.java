package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.simonpercic.bucket.model.SimpleObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketCacheArgsTest {

    Context context;
    BucketCache bucket;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws IOException {
        if (bucket != null) {
            bucket.cache.destroy();
        }
    }

    private BucketCache createCache() throws IOException {
        return BucketCache.builder(context, 1024 * 1024).build();
    }

    @Test
    public void testGetNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.get(null, SimpleObject.class);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testGetNullType() throws Exception {
        bucket = createCache();

        try {
            bucket.get("TEST_KEY", null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("typeOfT is null", e.getMessage());
        }
    }

    @Test
    public void testPutNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.put(null, new SimpleObject("TEST_VALUE"));
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testPutNullObject() throws Exception {
        bucket = createCache();

        try {
            bucket.put("TEST_KEY", null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("object is null", e.getMessage());
        }
    }

    @Test
    public void testContainsNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.contains(null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testRemoveNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.remove(null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }
}
