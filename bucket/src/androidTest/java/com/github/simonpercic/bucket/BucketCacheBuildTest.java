package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketCacheBuildTest {

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
    public void testBuildSize() throws Exception {
        int maxSizeBytes = 1024 * 1024;
        bucket = BucketCache.builder(context, maxSizeBytes).build();

        assertNotNull(bucket);
        assertNotNull(bucket.gson);
        assertEquals(maxSizeBytes, bucket.cache.maxSizeBytes);
    }

    @Test
    public void testBuildPath() throws Exception {
        bucket = BucketCache.builder(context, 1024 * 1024).build();

        File path = new File(context.getCacheDir() + BucketCache.CACHE_DIR);

        assertNotNull(bucket);
        assertNotNull(bucket.gson);
        assertEquals(path, bucket.cache.cacheDir);
    }
}
