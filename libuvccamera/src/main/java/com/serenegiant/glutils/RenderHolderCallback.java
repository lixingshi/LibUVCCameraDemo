package com.serenegiant.glutils;

import android.view.Surface;

/**
 * Created by john on 2017/6/10.
 */

public interface RenderHolderCallback {
    void onCreate(Surface var1);

    void onFrameAvailable();

    void onDestroy();
}