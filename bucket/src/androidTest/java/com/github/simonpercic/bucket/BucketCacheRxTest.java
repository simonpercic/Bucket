package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.simonpercic.bucket.model.SimpleObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
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
public class BucketCacheRxTest {

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
    public void testGetRx() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        testObservable(bucket.<SimpleObject>getRx(key, SimpleObject.class), new Action1<SimpleObject>() {
            @Override public void call(SimpleObject simpleObject) {
                assertNotNull(simpleObject);
                assertEquals(value, simpleObject.getValue());
            }
        });
    }

    @Test
    public void testPutRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        testObservable(bucket.<Boolean>putRx(key, new SimpleObject(value)), new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertNotNull(aBoolean);
                assertTrue(aBoolean);

                try {
                    assertTrue(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    @Test
    public void testContainsRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        testObservable(bucket.<Boolean>containsRx(key), new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertNotNull(aBoolean);
                assertTrue(aBoolean);
            }
        });
    }

    @Test
    public void testNotContainsRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";

        testObservable(bucket.<Boolean>containsRx(key), new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertNotNull(aBoolean);
                assertFalse(aBoolean);
            }
        });
    }

    @Test
    public void testRemoveRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        testObservable(bucket.<Boolean>removeRx(key), new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertNotNull(aBoolean);
                assertTrue(aBoolean);

                try {
                    assertFalse(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    @Test
    public void testClearRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        testObservable(bucket.<Boolean>clearRx(), new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                assertNotNull(aBoolean);
                assertTrue(aBoolean);

                try {
                    assertFalse(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    private <T> void testObservable(Observable<T> observable, final Action1<T> assertAction)
            throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        observable.subscribe(new TestObserver<T>(latch) {
            @Override public void onNext(T t) {
                assertAction.call(t);
            }
        });

        if (!latch.await(400, TimeUnit.MILLISECONDS)) {
            fail();
        }
    }

    private static abstract class TestObserver<T> implements Observer<T> {

        private final CountDownLatch countDownLatch;

        public TestObserver(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override public void onCompleted() {
            countDownLatch.countDown();
        }

        @Override public void onError(Throwable e) {
            fail(e.getMessage());
        }
    }
}
