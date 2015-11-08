package com.github.simonpercic.bucket;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache.
 * License Apache 2.0
 */
final class SimpleDiskCache {

    private static final String ENCODING = "UTF-8";
    private static final int VALUE_IDX = 0;
    private static final Set<String> USED_DIRS = new HashSet<>();

    private DiskLruCache diskLruCache;

    final File cacheDir;
    final long maxSizeBytes;

    private SimpleDiskCache(String path, long maxSizeBytes) throws IOException {
        this.cacheDir = new File(path);
        this.maxSizeBytes = maxSizeBytes;

        String cachePath = cacheDir.getPath();

        if (USED_DIRS.contains(cachePath)) {
            throw new IllegalStateException(String.format("Cache directory %s was used before.", cachePath));
        }

        if (!cacheDir.exists() && !cacheDir.mkdir()) {
            throw new IOException("Failed to create cache directory!");
        }

        USED_DIRS.add(cachePath);

        diskLruCache = createDiskLruCache(cacheDir, maxSizeBytes);
    }

    static synchronized SimpleDiskCache create(String path, long maxSizeBytes) throws IOException {
        return new SimpleDiskCache(path, maxSizeBytes);
    }

    private static synchronized DiskLruCache createDiskLruCache(File cacheDir, long maxSizeBytes) throws IOException {
        return DiskLruCache.open(cacheDir, 1, 1, maxSizeBytes);
    }

    String get(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) {
            return null;
        }

        try {
            return snapshot.getString(VALUE_IDX);
        } finally {
            snapshot.close();
        }
    }

    void put(String key, String value) throws IOException {
        if (value.getBytes(ENCODING).length > diskLruCache.getMaxSize()) {
            throw new IOException("Object is larger than cache size");
        }

        OutputStream cos = null;
        try {
            cos = openStream(key);
            cos.write(value.getBytes(ENCODING));
        } finally {
            if (cos != null) {
                cos.close();
            }
        }
    }

    boolean contains(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) {
            return false;
        }

        snapshot.close();
        return true;
    }

    void remove(String key) throws IOException {
        diskLruCache.remove(toInternalKey(key));
    }

    void clear() throws IOException {
        diskLruCache.delete();
        diskLruCache = createDiskLruCache(cacheDir, maxSizeBytes);
    }

    void destroy() throws IOException {
        diskLruCache.delete();
        USED_DIRS.remove(cacheDir.getPath());
    }

    // region private helpers

    private OutputStream openStream(String key) throws IOException {
        Editor editor = diskLruCache.edit(toInternalKey(key));
        try {
            BufferedOutputStream bos = new BufferedOutputStream(editor.newOutputStream(VALUE_IDX));
            return new DiskLruCacheOutputStream(bos, editor);
        } catch (IOException e) {
            editor.abort();
            throw e;
        }
    }

    private static String toInternalKey(String key) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(key.getBytes(ENCODING));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion private helpers
}
