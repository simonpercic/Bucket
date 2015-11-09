package com.github.simonpercic.bucket.callback;

/**
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public interface BucketCallback extends BucketFailureCallback {
    void onSuccess();
}
