package com.github.simonpercic.bucket;

import android.content.Context;

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
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public final class BucketCache {

    static final String CACHE_DIR = "/Bucket";

    final SimpleDiskCache cache;
    final Gson gson;
    final Scheduler subscribeScheduler;
    final Scheduler observeScheduler;

    private BucketCache(SimpleDiskCache cache, Gson gson, Scheduler subscribeScheduler, Scheduler observeScheduler) {
        this.cache = cache;
        this.gson = gson;
        this.subscribeScheduler = subscribeScheduler;
        this.observeScheduler = observeScheduler;
    }

    // region synchronous methods

    public <T> T get(String key, Type typeOfT) throws IOException {
        checkGetArgs(key, typeOfT);

        String json = cache.get(key);

        if (!StringUtils.isEmpty(json)) {
            return gson.fromJson(json, typeOfT);
        }

        return null;
    }

    public void put(String key, Object object) throws IOException {
        checkPutArgs(key, object);

        String json = gson.toJson(object);
        cache.put(key, json);
    }

    public boolean contains(String key) throws IOException {
        checkKeyArg(key);

        return cache.contains(key);
    }

    public void remove(String key) throws IOException {
        checkKeyArg(key);

        cache.remove(key);
    }

    public void clear() throws IOException {
        cache.clear();
    }

    // endregion synchronous methods

    // region asynchronous methods

    public <T> void getAsync(String key, Type typeOfT, final BucketGetCallback<T> callback) {
        checkGetArgs(key, typeOfT);

        Observable<T> get = getRx(key, typeOfT);
        doAsync(get, callback);
    }

    public void putAsync(String key, Object object, final BucketCallback callback) {
        checkPutArgs(key, object);

        doAsync(putRx(key, object), callback);
    }

    public void containsAsync(String key, final BucketGetCallback<Boolean> callback) {
        checkKeyArg(key);

        doAsync(containsRx(key), callback);
    }

    public void removeAsync(String key, final BucketCallback callback) {
        checkKeyArg(key);

        doAsync(removeRx(key), callback);
    }

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

    public <T> Observable<T> getRx(final String key, final Type typeOfT) {
        checkGetArgs(key, typeOfT);

        return createObservable(new Callable<T>() {
            @Override public T call() throws Exception {
                return get(key, typeOfT);
            }
        });
    }

    public Observable<Boolean> putRx(final String key, final Object object) {
        checkPutArgs(key, object);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                put(key, object);
                return true;
            }
        });
    }

    public Observable<Boolean> containsRx(final String key) {
        checkKeyArg(key);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return contains(key);
            }
        });
    }

    public Observable<Boolean> removeRx(final String key) {
        checkKeyArg(key);

        return createObservable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                remove(key);
                return true;
            }
        });
    }

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

    public static Builder builder(Context context, long maxSizeBytes) {
        checkObjectArgumentNull(context, "context");

        return new Builder(context.getApplicationContext(), maxSizeBytes);
    }

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

        public Builder withGson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public Builder withSubscribeScheduler(Scheduler scheduler) {
            this.subscribeScheduler = scheduler;
            return this;
        }

        public Builder withObserveScheduler(Scheduler scheduler) {
            this.observeScheduler = scheduler;
            return this;
        }

        public synchronized BucketCache build() throws IOException {
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

            return new BucketCache(cache, gson, subscribeScheduler, observeScheduler);
        }
    }

    // endregion Builder
}
