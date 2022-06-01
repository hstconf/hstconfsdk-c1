package com.hd.screencapture.observer;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.hd.screencapture.ScreenCapture;


/**
 * Created by hd on 2018/5/14 .
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class ScreenCaptureObserver extends CaptureObserver implements LifecycleObserver {

    public ScreenCaptureObserver(ScreenCapture screenCapture) {
        super(screenCapture);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
        Log.d(TAG, "onResume");
        alive = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (config.isRelevanceLifecycle()) {
            alive = false;
            stopCapture();
        }
    }
}
