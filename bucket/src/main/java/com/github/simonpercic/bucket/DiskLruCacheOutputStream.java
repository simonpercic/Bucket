package com.github.simonpercic.bucket;

import android.support.annotation.NonNull;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache
 * License Apache 2.0
 */
class DiskLruCacheOutputStream extends FilterOutputStream {

    private final DiskLruCache.Editor editor;
    private boolean failed = false;

    DiskLruCacheOutputStream(OutputStream os, DiskLruCache.Editor editor) {
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

        if (closeException != null) {
            throw closeException;
        }
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
