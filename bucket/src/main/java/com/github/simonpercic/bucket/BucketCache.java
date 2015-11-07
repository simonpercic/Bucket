package com.github.simonpercic.bucket;

import android.content.Context;

import com.github.simonpercic.bucket.SimpleDiskCache.StringEntry;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class BucketCache {

    final SimpleDiskCache cache;
    final Gson gson;

    private BucketCache(SimpleDiskCache cache, Gson gson) {
        this.cache = cache;
        this.gson = gson;
    }

    // region synchronous methods

    public <T> T get(String key, Type typeOfT) throws IOException {
        StringEntry entry = cache.getString(key);

        if (entry != null) {
            String json = entry.getString();

            if (json != null && json.length() > 0) {
                return gson.fromJson(json, typeOfT);
            }
        }

        return null;
    }

    public void put(String key, Object object) throws IOException {
        String json = gson.toJson(object);
        cache.put(key, json);
    }

    public boolean contains(String key) throws IOException {
        return cache.contains(key);
    }

    // endregion synchronous methods

    public static Builder builder(Context context, long maxSizeBytes) {
        if (context == null) {
            throw new IllegalArgumentException("Context should not be null!");
        }

        return new Builder(context.getApplicationContext(), maxSizeBytes);
    }

    // region Builder

    public static class Builder {

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
            File cacheDir = new File(context.getCacheDir() + "/BucketCache");

            if (!cacheDir.exists()) {
                if (!cacheDir.mkdir()) {
                    throw new IOException("Failed to create cache directory!");
                }
            }

            SimpleDiskCache cache = SimpleDiskCache.open(cacheDir, 1, maxSizeBytes);

            if (gson == null) {
                gson = new Gson();
            }

            return new BucketCache(cache, gson);
        }
    }

    // endregion Builder
}