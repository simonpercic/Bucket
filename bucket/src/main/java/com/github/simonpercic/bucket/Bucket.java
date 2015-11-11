package com.github.simonpercic.bucket;

import android.content.Context;
import android.support.annotation.Nullable;

import com.github.simonpercic.bucket.callback.BucketCallback;
import com.github.simonpercic.bucket.callback.BucketFailureCallback;
import com.github.simonpercic.bucket.callback.BucketGetCallback;
import com.github.simonpercic.bucket.utils.StringUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Bucket - a disk cache.
 * Supported operations:
 * - get,
 * - put,
 * - contains,
 * - remove and
 * - clear
 * Contains synchronous, asynchronous and RxJava methods.
 * Create an instance through the Builder, obtained by calling {@link #builder(android.content.Context, long) builder}
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public final class Bucket {

    static final String CACHE_DIR = "/Bucket";

    final SimpleDiskCache cache;
    final Gson gson;
    final Scheduler subscribeScheduler;
    final Scheduler observeScheduler;

    private Bucket(SimpleDiskCache cache, Gson gson, Scheduler subscribeScheduler, Scheduler observeScheduler) {
        this.cache = cache;
        this.gson = gson;
        this.subscribeScheduler = subscribeScheduler;
        this.observeScheduler = observeScheduler;
    }

    // region synchronous methods

    /**
     * Get from cache.
     *
     * @param key key
     * @param typeOfT type of cache value
     * @param <T> T of cache value
     * @return cache value
     * @throws IOException
     */
    @Nullable
    public <T> T get(String key, Type typeOfT) throws IOException {
        checkGetArgs(key, typeOfT);

        String json = cache.get(key);

        if (!StringUtils.isEmpty(json)) {
            return gson.fromJson(json, typeOfT);
        }

        return null;
    }

    /**
     * Put value to cache.
     *
     * @param key key
     * @param object object
     * @throws IOException
     */
    public void put(String key, Object object) throws IOException {
        checkPutArgs(key, object);

        String json = gson.toJson(object);
        cache.put(key, json);
    }

    /**
     * Cache contains key.
     *
     * @param key key
     * @return <tt>true</tt> if cache contains key, <tt>false</tt> otherwise
     * @throws IOException
     */
    public boolean contains(String key) throws IOException {
        checkKeyArg(key);

        return cache.contains(key);
    }

    /**
     * Remove cache value.
     *
     * @param key key
     * @throws IOException
     */
    public void remove(String key) throws IOException {
        checkKeyArg(key);

        cache.remove(key);
    }

    /**
     * Clear all cache values.
     *
     * @throws IOException
     */
    public void clear() throws IOException {
        cache.clear();
    }

    // endregion synchronous methods

    // region asynchronous methods

    /**
     * Get from cache - async, using a callback.
     *
     * @param key key
     * @param typeOfT type of cache value
     * @param callback callback that will be invoked to return the value
     * @param <T> T of cache value
     */
    public <T> void getAsync(String key, Type typeOfT, final BucketGetCallback<T> callback) {
        checkGetArgs(key, typeOfT);

        Observable<T> get = getRx(key, typeOfT);
        doAsync(get, callback);
    }

    /**
     * Put value to cache - async, using a callback.
     *
     * @param key key
     * @param object object
     * @param callback callback that will be invoked to report status
     */
    public void putAsync(String key, Object object, final BucketCallback callback) {
        checkPutArgs(key, object);

        doAsync(putRx(key, object), callback);
    }

    /**
     * Cache contains key - async, using a callback.
     *
     * @param key key
     * @param callback callback that will be invoked to report contains state
     */
    public void containsAsync(String key, final BucketGetCallback<Boolean> callback) {
        checkKeyArg(key);

        doAsync(containsRx(key), callback);
    }

    /**
     * Remove cache value - async, using a callback.
     *
     * @param key key
     * @param callback callback that will be invoked to report status
     */
    public void removeAsync(String key, final BucketCallback callback) {
        checkKeyArg(key);

        doAsync(removeRx(key), callback);
    }

    /**
     * Clear all cache values - async, using a callback.
     *
     * @param callback callback that will be invoked to report status
     */
    public void clearAsync(final BucketCallback callback) {
        doAsync(clearRx(), callback);
    }

    private static void doAsync(Observable<Boolean> observable, final BucketCallback callback) {
        observable.subscribe(new Action1<Boolean>() {
            @Override public void call(Boolean aBoolean) {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        }, asyncOnError(callback));
    }

    private static <T> void doAsync(Observable<T> observable, final BucketGetCallback<T> callback) {
        observable.subscribe(new Action1<T>() {
            @Override public void call(T t) {
                if (callback != null) {
                    callback.onSuccess(t);
                }
            }
        }, asyncOnError(callback));
    }

    private static Action1<Throwable> asyncOnError(final BucketFailureCallback callback) {
        return new Action1<Throwable>() {
            @Override public void call(Throwable throwable) {
                if (callback != null) {
                    callback.onFailure(throwable);
                }
            }
        };
    }

    // endregion asynchronous methods

    // region Reactive methods

    /**
     * Get from cache - reactive, using an Observable.
     *
     * @param key key
     * @param typeOfT type of cache value
     * @param <T> T of cache value
     * @return Observable that emits the cache value
     */
    public <T> Observable<T> getRx(final String key, final Type typeOfT) {
        checkGetArgs(key, typeOfT);

        return createObservable(new Callable<T>() {
            @Override public T call() throws Exception {
                return get(key, typeOfT);
            }
        });
    }

    /**
     * Put value to cache - reactive, using an Observable.
     *
     * @param key key
     * @param object object
     * @return Observable that emits <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public Observable<Boolean> putRx(final String key, final Object object) {
        checkPutArgs(key, object);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                put(key, object);
                return true;
            }
        });
    }

    /**
     * Cache contains key - reactive, using an Observable.
     *
     * @param key key
     * @return Observable that emits <tt>true</tt> if cache contains key, <tt>false</tt> otherwise
     */
    public Observable<Boolean> containsRx(final String key) {
        checkKeyArg(key);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return contains(key);
            }
        });
    }

    /**
     * Remove cache value - reactive, using an Observable.
     *
     * @param key key
     * @return Observable that emits <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public Observable<Boolean> removeRx(final String key) {
        checkKeyArg(key);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                remove(key);
                return true;
            }
        });
    }

    /**
     * Clear all cache values - reactive, using an Observable.
     *
     * @return Observable that emits <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public Observable<Boolean> clearRx() {
        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                clear();
                return true;
            }
        });
    }

    private <T> Observable<T> createObservable(final Callable<T> func) {
        return Observable.create(new OnSubscribe<T>() {
            @Override public void call(Subscriber<? super T> subscriber) {
                try {
                    T object = func.call();
                    subscriber.onNext(object);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(subscribeScheduler).observeOn(observeScheduler);
    }

    // endregion Reactive methods

    // region args checks

    private static void checkGetArgs(String key, Type typeOfT) {
        checkStringArgumentEmpty(key, "key");
        checkObjectArgumentNull(typeOfT, "typeOfT");
    }

    private static void checkPutArgs(String key, Object object) {
        checkStringArgumentEmpty(key, "key");
        checkObjectArgumentNull(object, "object");
    }

    private static void checkKeyArg(String key) {
        checkStringArgumentEmpty(key, "key");
    }

    private static void checkStringArgumentEmpty(String value, String name) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(name + " is null or empty");
        }
    }

    private static void checkObjectArgumentNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is null");
        }
    }

    // endregion args checks

    // region Builder

    /**
     * Returns a Builder.
     *
     * @param context context
     * @param maxSizeBytes max size of cache in bytes
     * @return Builder instance
     */
    public static Builder builder(Context context, long maxSizeBytes) {
        checkObjectArgumentNull(context, "context");

        return new Builder(context.getApplicationContext(), maxSizeBytes);
    }

    /**
     * Bucket Builder.
     */
    public static final class Builder {

        private final Context context;
        private final long maxSizeBytes;

        private Gson gson;
        private Scheduler subscribeScheduler;
        private Scheduler observeScheduler;

        private Builder(Context context, long maxSizeBytes) {
            this.context = context;
            this.maxSizeBytes = maxSizeBytes;
        }

        /**
         * Set a custom Gson instance.
         *
         * @param gson gson instance
         * @return Builder
         */
        public Builder withGson(Gson gson) {
            this.gson = gson;
            return this;
        }

        /**
         * Set a custom subscribeOn scheduler to control the thread the background processing takes place.
         * Defaults to the IO thread from Schedulers.io()
         *
         * @param scheduler scheduler for background processing
         * @return Builder
         */
        public Builder withSubscribeScheduler(Scheduler scheduler) {
            this.subscribeScheduler = scheduler;
            return this;
        }

        /**
         * Set a custom observeOn scheduler to control the thread that receives the updates.
         * Defaults to the Android main thread from AndroidSchedulers.mainThread()
         *
         * @param scheduler scheduler to receive the updates
         * @return Builder
         */
        public Builder withObserveScheduler(Scheduler scheduler) {
            this.observeScheduler = scheduler;
            return this;
        }

        /**
         * Build the Bucket.
         *
         * @return Bucket instance
         * @throws IOException
         */
        public synchronized Bucket build() throws IOException {
            String cachePath = context.getCacheDir() + CACHE_DIR;

            SimpleDiskCache cache = SimpleDiskCache.create(cachePath, maxSizeBytes);

            if (gson == null) {
                gson = new Gson();
            }

            if (subscribeScheduler == null) {
                subscribeScheduler = Schedulers.io();
            }

            if (observeScheduler == null) {
                observeScheduler = AndroidSchedulers.mainThread();
            }

            return new Bucket(cache, gson, subscribeScheduler, observeScheduler);
        }
    }

    // endregion Builder
}
