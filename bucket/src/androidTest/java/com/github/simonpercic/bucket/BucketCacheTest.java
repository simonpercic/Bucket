package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketCacheTest {

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
    public void testBuildNoException() throws Exception {
        bucket = BucketCache.builder(context, 1024 * 1024).build();

        assertNotNull(bucket);
        assertNotNull(bucket.cache);
        assertNotNull(bucket.gson);
    }

    @Test
    public void testBuildGson() throws Exception {
        Gson gson = new Gson();

        bucket = BucketCache.builder(context, 1024 * 1024).withGson(gson).build();

        assertNotNull(bucket);
        assertNotNull(bucket.gson);
        assertEquals(gson, bucket.gson);
    }

    @Test
    public void testGetNoValue() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        SimpleObject cached = bucket.get(key, SimpleObject.class);
        assertNull(cached);
    }

    @Test
    public void testContainsNoValue() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        boolean contains = bucket.contains(key);
        assertFalse(contains);
    }

    @Test
    public void testSimple() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        SimpleObject cached = bucket.get(key, SimpleObject.class);
        assertNotNull(cached);

        assertEquals(value, cached.value);
    }

    @Test
    public void testWrapped() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        String simpleValue = "TEST_SIMPLE_VALUE";
        String wrappedValue = "TEST_WRAPPED_VALUE";

        SimpleObject simple = new SimpleObject(simpleValue);

        WrappedObject wrapped = new WrappedObject();
        wrapped.object = simple;
        wrapped.value = wrappedValue;

        bucket.put(key, wrapped);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        WrappedObject cached = bucket.get(key, WrappedObject.class);
        assertNotNull(cached);
        assertNotNull(cached.object);

        assertEquals(wrapped.value, cached.value);
        assertEquals(wrapped.object.value, cached.object.value);
    }

    @Test
    public void testGeneric() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        String simpleValue = "TEST_SIMPLE_VALUE";
        String wrappedValue = "TEST_WRAPPED_VALUE";
        String genericValue = "TEST_GENERIC_VALUE";

        SimpleObject simple = new SimpleObject(simpleValue);

        WrappedObject wrapped = new WrappedObject();
        wrapped.object = simple;
        wrapped.value = wrappedValue;

        GenericObject<WrappedObject> generic = new GenericObject<>();
        generic.object = wrapped;
        generic.value = genericValue;

        bucket.put(key, generic);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        Type type = new TypeToken<GenericObject<WrappedObject>>() {
        }.getType();

        GenericObject<WrappedObject> cached = bucket.get(key, type);
        assertNotNull(cached);
        assertNotNull(cached.object);
        assertNotNull(cached.object.object);

        assertEquals(generic.value, cached.value);
        assertEquals(wrapped.value, cached.object.value);
        assertEquals(simple.value, cached.object.object.value);
    }

    @Test
    public void testCollection() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        String value1 = "TEST_VALUE_1";
        String value2 = "TEST_VALUE_2";

        List<SimpleObject> list = Arrays.asList(new SimpleObject(value1), new SimpleObject(value2));

        bucket.put(key, list);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        Type type = new TypeToken<List<SimpleObject>>() {
        }.getType();

        List<SimpleObject> cached = bucket.get(key, type);
        assertNotNull(cached);

        assertEquals(2, cached.size());
        assertNotNull(cached.get(0));
        assertNotNull(cached.get(1));
        assertEquals(value1, cached.get(0).value);
        assertEquals(value2, cached.get(1).value);
    }

    private static class SimpleObject {
        private String value;

        public SimpleObject(String value) {
            this.value = value;
        }
    }

    private static class WrappedObject {
        private SimpleObject object;

        private String value;
    }

    private static class GenericObject<T> {
        private T object;

        private String value;
    }
}
