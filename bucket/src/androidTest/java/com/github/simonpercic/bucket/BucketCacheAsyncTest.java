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

import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketCacheAsyncTest {

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
        return BucketCache.builder(context, 1024 * 1024)
                .withSubscribeScheduler(Schedulers.immediate())
                .withObserveScheduler(Schedulers.immediate())
                .build();
    }

    @Test
    public void testGetAsync() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        bucket.getAsync(key, SimpleObject.class, new TestBucketGetCallback<>(new Action1<SimpleObject>() {
            @Override public void call(SimpleObject simpleObject) {
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

        bucket.<Boolean>containsAsync(key, new TestBucketGetCallback<>(new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertTrue(aBoolean);
            }
        }));
    }

    @Test
    public void testNotContainsAsync() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";

        bucket.<Boolean>containsAsync(key, new TestBucketGetCallback<>(new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
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

        private final Action1<T> assertAction;

        private TestBucketGetCallback(Action1<T> assertAction) {
            this.assertAction = assertAction;
        }

        @Override public void onSuccess(T t) {
            assertNotNull(t);
            assertAction.call(t);
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
