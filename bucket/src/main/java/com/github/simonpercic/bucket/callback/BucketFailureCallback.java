package com.github.simonpercic.bucket.callback;

/**
 * Bucket failure callback.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public interface BucketFailureCallback {

    /**
     * Called on failure.
     *
     * @param throwable throwable
     */
    void onFailure(Throwable throwable);
}
