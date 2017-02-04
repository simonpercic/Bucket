package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketBuildTest {

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

    @Test
    public void testBuildNoException() throws Exception {
        bucket = Bucket.builder(context, 1024 * 1024).build();

        assertNotNull(bucket);
        assertNotNull(bucket.cache);
        assertNotNull(bucket.gson);
        assertNotNull(bucket.subscribeScheduler);
        assertNotNull(bucket.observeScheduler);
    }

    @Test
    public void testBuildGson() throws Exception {
        Gson gson = new Gson();

        bucket = Bucket.builder(context, 1024 * 1024).withGson(gson).build();

        assertNotNull(bucket);
        assertNotNull(bucket.gson);
        assertEquals(gson, bucket.gson);
    }

    @Test
    public void testBuildGsonNull() throws Exception {
        bucket = Bucket.builder(context, 1024 * 1024).withGson(null).build();

        assertNotNull(bucket);
        assertNotNull(bucket.gson);
    }

    @Test
    public void testBuildSubscribeScheduler() throws Exception {
        Scheduler scheduler = Schedulers.trampoline();
        bucket = Bucket.builder(context, 1024 * 1024).withSubscribeScheduler(scheduler).build();

        assertNotNull(bucket);
        assertNotNull(bucket.subscribeScheduler);
        assertEquals(scheduler, bucket.subscribeScheduler);
    }

    @Test
    public void testBuildSubscribeSchedulerNull() throws Exception {
        bucket = Bucket.builder(context, 1024 * 1024).withSubscribeScheduler(null).build();

        assertNotNull(bucket);
        assertNotNull(bucket.subscribeScheduler);
    }

    @Test
    public void testBuildObserveScheduler() throws Exception {
        Scheduler scheduler = Schedulers.trampoline();
        bucket = Bucket.builder(context, 1024 * 1024).withObserveScheduler(scheduler).build();

        assertNotNull(bucket);
        assertNotNull(bucket.observeScheduler);
        assertEquals(scheduler, bucket.observeScheduler);
    }

    @Test
    public void testBuildObserveSchedulerNull() throws Exception {
        bucket = Bucket.builder(context, 1024 * 1024).withObserveScheduler(null).build();

        assertNotNull(bucket);
        assertNotNull(bucket.observeScheduler);
    }

    @Test
    public void testBuildSize() throws Exception {
        int maxSizeBytes = 1024 * 1024;
        bucket = Bucket.builder(context, maxSizeBytes).build();

        assertNotNull(bucket);
        assertEquals(maxSizeBytes, bucket.cache.maxSizeBytes);
    }

    @Test
    public void testBuildPath() throws Exception {
        bucket = Bucket.builder(context, 1024 * 1024).build();

        File path = new File(context.getCacheDir() + Bucket.CACHE_DIR);

        assertNotNull(bucket);
        assertEquals(path, bucket.cache.cacheDir);
    }
}
