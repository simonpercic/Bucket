package com.github.simonpercic.bucket;

import android.content.Context;

import com.github.simonpercic.bucket.utils.StringUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class BucketCache {

    static final String CACHE_DIR = "/Bucket";

    final SimpleDiskCache cache;
    final Gson gson;

    private BucketCache(SimpleDiskCache cache, Gson gson) {
        this.cache = cache;
        this.gson = gson;
    }

    // region synchronous methods

    public <T> T get(String key, Type typeOfT) throws IOException {
        checkStringArgumentEmpty(key, "key");
        checkObjectArgumentNull(typeOfT, "typeOfT");

        String json = cache.get(key);

        if (!StringUtils.isEmpty(json)) {
            return gson.fromJson(json, typeOfT);
        }

        return null;
    }

    public void put(String key, Object object) throws IOException {
        checkStringArgumentEmpty(key, "key");
        checkObjectArgumentNull(object, "object");

        String json = gson.toJson(object);
        cache.put(key, json);
    }

    public boolean contains(String key) throws IOException {
        checkStringArgumentEmpty(key, "key");

        return cache.contains(key);
    }

    public void remove(String key) throws IOException {
        checkStringArgumentEmpty(key, "key");

        cache.remove(key);
    }

    public void clear() throws IOException {
        cache.clear();
    }

    // endregion synchronous methods

    // region private helpers

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

    // endregion private helpers

    // region Builder

    public static Builder builder(Context context, long maxSizeBytes) {
        checkObjectArgumentNull(context, "context");

        return new Builder(context.getApplicationContext(), maxSizeBytes);
    }

    public static final class Builder {

        private final Context context;
        private final long maxSizeBytes;

        private Gson gson;

        private Builder(Context context, long maxSizeBytes) {
            this.context = context;
            this.maxSizeBytes = maxSizeBytes;
        }

        public Builder withGson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public synchronized BucketCache build() throws IOException {
            String cachePath = context.getCacheDir() + CACHE_DIR;

            SimpleDiskCache cache = SimpleDiskCache.create(cachePath, maxSizeBytes);

            if (gson == null) {
                gson = new Gson();
            }

            return new BucketCache(cache, gson);
        }
    }

    // endregion Builder
}
