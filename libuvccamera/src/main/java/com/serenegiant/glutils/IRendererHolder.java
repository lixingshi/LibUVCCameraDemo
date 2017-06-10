package com.serenegiant.glutils;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * Created by john on 2017/6/10.
 */

public interface IRendererHolder extends IRendererCommon {
    boolean isRunning();

    void release();

    Surface getSurface();

    SurfaceTexture getSurfaceTexture();

    void reset();

    void resize(int var1, int var2);

    void addSurface(int var1, Object var2, boolean var3);

    void addSurface(int var1, Object var2, boolean var3, int var4);

    void removeSurface(int var1);

    boolean isEnabled(int var1);

    void setEnabled(int var1, boolean var2);

    void requestFrame();

    int getCount();

    void captureStillAsync(String var1);

    void captureStill(String var1);
}
