package com.github.simonpercic.bucket.callback;

/**
 * Bucket get callback.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public interface BucketGetCallback<T> extends BucketFailureCallback {

    /**
     * Called on success. Returns the value.
     *
     * @param object returned object
     */
    void onSuccess(T object);
}
