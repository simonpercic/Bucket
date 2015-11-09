package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.simonpercic.bucket.model.GenericObject;
import com.github.simonpercic.bucket.model.SimpleObject;
import com.github.simonpercic.bucket.model.WrappedObject;
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
public class BucketSyncTest {

    Context context;
    Bucket bucket;

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

    private Bucket createCache() throws IOException {
        return Bucket.builder(context, 1024 * 1024).build();
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

        assertEquals(value, cached.getValue());
    }

    @Test
    public void testWrapped() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        String simpleValue = "TEST_SIMPLE_VALUE";
        String wrappedValue = "TEST_WRAPPED_VALUE";

        SimpleObject simple = new SimpleObject(simpleValue);

        WrappedObject wrapped = new WrappedObject();
        wrapped.setObject(simple);
        wrapped.setValue(wrappedValue);

        bucket.put(key, wrapped);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        WrappedObject cached = bucket.get(key, WrappedObject.class);
        assertNotNull(cached);
        assertNotNull(cached.getObject());

        assertEquals(wrapped.getValue(), cached.getValue());
        assertEquals(wrapped.getObject().getValue(), cached.getObject().getValue());
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
        wrapped.setObject(simple);
        wrapped.setValue(wrappedValue);

        GenericObject<WrappedObject> generic = new GenericObject<>();
        generic.setObject(wrapped);
        generic.setValue(genericValue);

        bucket.put(key, generic);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        Type type = new TypeToken<GenericObject<WrappedObject>>() {
        }.getType();

        GenericObject<WrappedObject> cached = bucket.get(key, type);
        assertNotNull(cached);
        assertNotNull(cached.getObject());
        assertNotNull(cached.getObject().getObject());

        assertEquals(generic.getValue(), cached.getValue());
        assertEquals(wrapped.getValue(), cached.getObject().getValue());
        assertEquals(simple.getValue(), cached.getObject().getObject().getValue());
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
        assertEquals(value1, cached.get(0).getValue());
        assertEquals(value2, cached.get(1).getValue());
    }

    @Test
    public void testRemove() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        String value = "TEST_VALUE";

        SimpleObject simple = new SimpleObject(value);

        bucket.put(key, simple);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        bucket.remove(key);

        contains = bucket.contains(key);
        assertFalse(contains);

        SimpleObject cached = bucket.get(key, SimpleObject.class);
        assertNull(cached);
    }

    @Test
    public void testRemoveNoValue() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        bucket.remove(key);

        assertFalse(bucket.contains(key));
    }

    @Test
    public void testClearSingleValue() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";

        String value = "TEST_VALUE";

        SimpleObject simple = new SimpleObject(value);

        bucket.put(key, simple);

        boolean contains = bucket.contains(key);
        assertTrue(contains);

        bucket.clear();

        contains = bucket.contains(key);
        assertFalse(contains);

        SimpleObject cached = bucket.get(key, SimpleObject.class);
        assertNull(cached);
    }

    @Test
    public void testClearMultipleValues() throws Exception {
        bucket = createCache();

        String key1 = "TEST_KEY_1";
        String key2 = "TEST_KEY_2";

        String value1 = "TEST_VALUE_1";
        String value2 = "TEST_VALUE_2";

        SimpleObject simple1 = new SimpleObject(value1);
        SimpleObject simple2 = new SimpleObject(value2);

        bucket.put(key1, simple1);
        bucket.put(key2, simple2);

        boolean contains = bucket.contains(key1);
        assertTrue(contains);

        contains = bucket.contains(key2);
        assertTrue(contains);

        bucket.clear();

        contains = bucket.contains(key1);
        assertFalse(contains);

        contains = bucket.contains(key2);
        assertFalse(contains);

        SimpleObject cached1 = bucket.get(key1, SimpleObject.class);
        assertNull(cached1);

        SimpleObject cached2 = bucket.get(key2, SimpleObject.class);
        assertNull(cached2);
    }

    @Test
    public void testClearWorksAfter() throws Exception {
        bucket = createCache();

        String key1 = "TEST_KEY_1";
        String value1 = "TEST_VALUE_1";
        bucket.put(key1, new SimpleObject(value1));

        bucket.clear();

        String key2 = "TEST_KEY_2";
        String value2 = "TEST_VALUE_2";
        bucket.put(key2, new SimpleObject(value2));

        assertTrue(bucket.contains(key2));

        SimpleObject cached2 = bucket.get(key2, SimpleObject.class);
        assertNotNull(cached2);
        assertEquals(value2, cached2.getValue());

        String value1after = "TEST_VALUE_1_AFTER";
        bucket.put(key1, new SimpleObject(value1after));

        assertTrue(bucket.contains(key1));

        SimpleObject cached1 = bucket.get(key1, SimpleObject.class);
        assertNotNull(cached1);
        assertEquals(value1after, cached1.getValue());
    }
}
