package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.simonpercic.bucket.callback.BucketCallback;
import com.github.simonpercic.bucket.callback.BucketGetCallback;
import com.github.simonpercic.bucket.model.SimpleObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketAsyncTest {

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
        return Bucket.builder(context, 1024 * 1024)
                .withSubscribeScheduler(Schedulers.trampoline())
                .withObserveScheduler(Schedulers.trampoline())
                .build();
    }

    @Test
    public void testGetAsync() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        bucket.getAsync(key, SimpleObject.class, new TestBucketGetCallback<>(new Consumer<SimpleObject>() {
            @Override
            public void accept(SimpleObject simpleObject) {
                assertEquals(value, simpleObject.getValue());
            }
        }));
    }

    @Test
    public void testPutAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.<Boolean>putAsync(key, new SimpleObject(value), new TestBucketCallback() {
            @Override public void onSuccess() {
                try {
                    assertTrue(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    @Test
    public void testContainsAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        bucket.<Boolean>containsAsync(key, new TestBucketGetCallback<>(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                assertTrue(aBoolean);
            }
        }));
    }

    @Test
    public void testNotContainsAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";

        bucket.<Boolean>containsAsync(key, new TestBucketGetCallback<>(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                assertFalse(aBoolean);
            }
        }));
    }

    @Test
    public void testRemoveAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        bucket.<Boolean>removeAsync(key, new TestBucketCallback() {
            @Override public void onSuccess() {
                try {
                    assertFalse(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    @Test
    public void testClearAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        bucket.<Boolean>clearAsync(new TestBucketCallback() {
            @Override public void onSuccess() {
                try {
                    assertFalse(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    private static class TestBucketGetCallback<T> implements BucketGetCallback<T> {

        private final Consumer<T> assertAction;

        private TestBucketGetCallback(Consumer<T> assertAction) {
            this.assertAction = assertAction;
        }

        @Override public void onSuccess(T t) throws Exception {
            assertNotNull(t);
            assertAction.accept(t);
        }

        @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
        }
    }

    private static abstract class TestBucketCallback implements BucketCallback {

        @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
        }
    }
}
