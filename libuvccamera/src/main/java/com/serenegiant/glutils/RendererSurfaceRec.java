package com.serenegiant.glutils;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by john on 2017/6/10.
 */

class RendererSurfaceRec {
    private Object mSurface;
    private EGLBase.IEglSurface mTargetSurface;
    final float[] mMvpMatrix;
    protected volatile boolean mEnable;

    static RendererSurfaceRec newInstance(EGLBase egl, Object surface, int maxFps) {
        return (RendererSurfaceRec)(maxFps > 0?new RendererSurfaceRec.RendererSurfaceRecHasWait(egl, surface, maxFps):new RendererSurfaceRec(egl, surface));
    }

    private RendererSurfaceRec(EGLBase egl, Object surface) {
        this.mMvpMatrix = new float[16];
        this.mEnable = true;
        this.mSurface = surface;
        this.mTargetSurface = egl.createFromSurface(surface);
        Matrix.setIdentityM(this.mMvpMatrix, 0);
    }

    public void release() {
        if(this.mTargetSurface != null) {
            this.mTargetSurface.release();
            this.mTargetSurface = null;
        }

        this.mSurface = null;
    }

    public boolean isValid() {
        return this.mTargetSurface != null && this.mTargetSurface.isValid();
    }

    public boolean isEnabled() {
        return this.mEnable;
    }

    public void setEnabled(boolean enable) {
        this.mEnable = enable;
    }

    public boolean canDraw() {
        return this.mEnable;
    }

    public void draw(GLDrawer2D drawer, int textId, float[] texMatrix) {
        this.mTargetSurface.makeCurrent();
        GLES20.glClear(16384);
        drawer.setMvpMatrix(this.mMvpMatrix, 0);
        drawer.draw(textId, texMatrix, 0);
        this.mTargetSurface.swap();
    }

    public void makeCurrent() {
        this.mTargetSurface.makeCurrent();
    }

    public void swap() {
        this.mTargetSurface.swap();
    }

    private static class RendererSurfaceRecHasWait extends RendererSurfaceRec {
        private long mNextDraw;
        private final long mIntervalsNs;

        private RendererSurfaceRecHasWait(EGLBase egl, Object surface, int maxFps) {
            super(egl, surface);
            this.mIntervalsNs = 1000000000L / (long)maxFps;
            this.mNextDraw = System.nanoTime() + this.mIntervalsNs;
        }

        public boolean canDraw() {
            return this.mEnable && System.nanoTime() > this.mNextDraw;
        }

        public void draw(GLDrawer2D drawer, int textId, float[] texMatrix) {
            this.mNextDraw = System.nanoTime() + this.mIntervalsNs;
            super.draw(drawer, textId, texMatrix);
        }
    }
}
