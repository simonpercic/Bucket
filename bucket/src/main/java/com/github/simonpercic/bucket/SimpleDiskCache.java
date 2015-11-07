package com.github.simonpercic.bucket;

import android.support.annotation.NonNull;

import com.jakewharton.disklrucache.DiskLruCache;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache
 * License Apache 2.0
 */
public class SimpleDiskCache {

    private static final int VALUE_IDX = 0;
    private static final int METADATA_IDX = 1;
    private static final Set<String> USED_DIRS = new HashSet<>();

    private DiskLruCache diskLruCache;

    private final File cacheDir;
    private final long maxSizeBytes;

    private SimpleDiskCache(String path, long maxSizeBytes) throws IOException {
        this.cacheDir = new File(path);
        this.maxSizeBytes = maxSizeBytes;

        String cachePath = cacheDir.getPath();

        if (USED_DIRS.contains(cachePath)) {
            throw new IllegalStateException(String.format("Cache directory %s was used before.", cachePath));
        }

        if (!cacheDir.exists()) {
            if (!cacheDir.mkdir()) {
                throw new IOException("Failed to create cache directory!");
            }
        }

        USED_DIRS.add(cachePath);

        diskLruCache = createDiskLruCache(cacheDir, maxSizeBytes);
    }

    public static synchronized SimpleDiskCache create(String path, long maxSizeBytes) throws IOException {
        return new SimpleDiskCache(path, maxSizeBytes);
    }

    private static synchronized DiskLruCache createDiskLruCache(File cacheDir, long maxSizeBytes) throws IOException {
        return DiskLruCache.open(cacheDir, 1, 2, maxSizeBytes);
    }

    public void clear() throws IOException {
        diskLruCache.delete();
        diskLruCache = createDiskLruCache(cacheDir, maxSizeBytes);
    }

    void destroy() throws IOException {
        diskLruCache.delete();
        USED_DIRS.remove(cacheDir.getPath());
    }

    public StringEntry getString(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return null;

        try {
            return new StringEntry(snapshot.getString(VALUE_IDX));
        } finally {
            snapshot.close();
        }
    }

    public void put(String key, String value) throws IOException {
        if (value.getBytes().length > diskLruCache.getMaxSize()) {
            throw new IOException("");
        }

        put(key, value, new HashMap<String, Serializable>());
    }

    public boolean contains(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return false;

        snapshot.close();
        return true;
    }

    private OutputStream openStream(String key, Map<String, ? extends Serializable> metadata) throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(toInternalKey(key));
        try {
            writeMetadata(metadata, editor);
            BufferedOutputStream bos = new BufferedOutputStream(editor.newOutputStream(VALUE_IDX));
            return new CacheOutputStream(bos, editor);
        } catch (IOException e) {
            editor.abort();
            throw e;
        }
    }

    public void put(String key, String value, Map<String, ? extends Serializable> annotations) throws IOException {
        OutputStream cos = null;
        try {
            cos = openStream(key, annotations);
            cos.write(value.getBytes());
        } finally {
            if (cos != null) cos.close();
        }
    }

    public void delete(String key) throws IOException {
        diskLruCache.remove(toInternalKey(key));
    }

    private void writeMetadata(Map<String, ? extends Serializable> metadata, DiskLruCache.Editor editor)
            throws IOException {

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(
                    editor.newOutputStream(METADATA_IDX)));
            oos.writeObject(metadata);
        } finally {
            IOUtils.closeQuietly(oos);
        }
    }

    private String toInternalKey(String key) {
        return md5(key);
    }

    private String md5(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes("UTF-8"));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    private class CacheOutputStream extends FilterOutputStream {

        private final DiskLruCache.Editor editor;
        private boolean failed = false;

        private CacheOutputStream(OutputStream os, DiskLruCache.Editor editor) {
            super(os);
            this.editor = editor;
        }

        @Override
        public void close() throws IOException {
            IOException closeException = null;
            try {
                super.close();
            } catch (IOException e) {
                closeException = e;
            }

            if (failed) {
                editor.abort();
            } else {
                editor.commit();
            }

            if (closeException != null) throw closeException;
        }

        @Override
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(int oneByte) throws IOException {
            try {
                super.write(oneByte);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(@NonNull byte[] buffer) throws IOException {
            try {
                super.write(buffer);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(@NonNull byte[] buffer, int offset, int length) throws IOException {
            try {
                super.write(buffer, offset, length);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }
    }

    public static class StringEntry {
        private final String string;

        public StringEntry(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }
}
