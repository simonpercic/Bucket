package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.simonpercic.bucket.model.SimpleObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketRxTest {

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
    public void testGetRx() throws Exception {
        bucket = createCache();

        String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        bucket.put(key, new SimpleObject(value));

        testObservable(bucket.<SimpleObject>getRx(key, SimpleObject.class), new Consumer<SimpleObject>() {
            @Override
            public void accept(SimpleObject simpleObject) {
                assertEquals(value, simpleObject.getValue());
            }
        });
    }

    @Test
    public void testPutRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";
        final String value = "TEST_VALUE";

        testObservable(bucket.<Boolean>putRx(key, new SimpleObject(value)), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
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

        testObservable(bucket.<Boolean>containsRx(key), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                assertTrue(aBoolean);
            }
        });
    }

    @Test
    public void testNotContainsRx() throws Exception {
        bucket = createCache();

        final String key = "TEST_KEY";

        testObservable(bucket.<Boolean>containsRx(key), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
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

        testObservable(bucket.<Boolean>removeRx(key), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
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

        testObservable(bucket.<Boolean>clearRx(), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                assertTrue(aBoolean);

                try {
                    assertFalse(bucket.contains(key));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    private static <T> void testObservable(Observable<T> observable, Consumer<T> assertAction) throws Exception {
        TestObserver<T> testSubscriber = observable.test();

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);

        List<List<Object>> onNextEvents = testSubscriber.getEvents();
        assertEquals(1, onNextEvents.get(0).size());
        assertEquals(0, onNextEvents.get(1).size());
        assertEquals(1, onNextEvents.get(2).size());

        T value = (T)onNextEvents.get(0).get(0);
        assertNotNull(value);
        assertAction.accept(value);
    }
}
