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

    // region synchronous methods

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

    // endregion synchronous methods

    // region asynchronous methods

    @Test
    public void testGetAsyncNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.getAsync(null, SimpleObject.class, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testGetAsyncNullType() throws Exception {
        bucket = createCache();

        try {
            bucket.getAsync("TEST_KEY", null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("typeOfT is null", e.getMessage());
        }
    }

    @Test
    public void testPutAsyncNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.putAsync(null, new SimpleObject("TEST_VALUE"), null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testPutAsyncNullObject() throws Exception {
        bucket = createCache();

        try {
            bucket.putAsync("TEST_KEY", null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("object is null", e.getMessage());
        }
    }

    @Test
    public void testContainsAsyncNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.containsAsync(null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testRemoveAsyncNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.removeAsync(null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    // endregion asynchronous methods

    // region Reactive methods

    @Test
    public void testGetRxKey() throws Exception {
        bucket = createCache();

        try {
            bucket.getRx(null, SimpleObject.class);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testGetRxNullType() throws Exception {
        bucket = createCache();

        try {
            bucket.getRx("TEST_KEY", null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("typeOfT is null", e.getMessage());
        }
    }

    @Test
    public void testPutRxNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.putRx(null, new SimpleObject("TEST_VALUE"));
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testPutRxNullObject() throws Exception {
        bucket = createCache();

        try {
            bucket.putRx("TEST_KEY", null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("object is null", e.getMessage());
        }
    }

    @Test
    public void testContainsRxNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.containsRx(null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    @Test
    public void testRemoveRxNullKey() throws Exception {
        bucket = createCache();

        try {
            bucket.removeRx(null);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertThat(e, new InstanceOf(IllegalArgumentException.class));
            assertEquals("key is null or empty", e.getMessage());
        }
    }

    // endregion Reactive methods
}
