package com.serenegiant.glutils;

/**
 * Created by john on 2017/6/10.
 */

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.EGLBase.IContext;
import com.serenegiant.glutils.EGLBase.IEglSurface;

public final class RenderHandler extends Handler {
    private static final String TAG = "RenderHandler";
    private static final int MSG_RENDER_SET_GLCONTEXT = 1;
    private static final int MSG_RENDER_DRAW = 2;
    private static final int MSG_CHECK_VALID = 3;
    private static final int MSG_RENDER_QUIT = 9;
    private int mTexId;
    private final RenderHandler.RenderThread mThread;

    public static RenderHandler createHandler() {
        return createHandler("RenderThread");
    }

    public static final RenderHandler createHandler(String name) {
        RenderHandler.RenderThread thread = new RenderHandler.RenderThread(name);
        thread.start();
        return thread.getHandler();
    }

    public final void setEglContext(IContext sharedContext, int tex_id, Object surface, boolean isRecordable) {
        if(!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder)) {
            throw new RuntimeException("unsupported window type:" + surface);
        } else {
            this.mTexId = tex_id;
            this.sendMessage(this.obtainMessage(1, isRecordable?1:0, 0, new RenderHandler.ContextParams(sharedContext, surface)));
        }
    }

    public final void draw() {
        this.sendMessage(this.obtainMessage(2, this.mTexId, 0, (Object)null));
    }

    public final void draw(int tex_id) {
        this.sendMessage(this.obtainMessage(2, tex_id, 0, (Object)null));
    }

    public final void draw(float[] tex_matrix) {
        this.sendMessage(this.obtainMessage(2, this.mTexId, 0, tex_matrix));
    }

    public final void draw(int tex_id, float[] tex_matrix) {
        this.sendMessage(this.obtainMessage(2, tex_id, 0, tex_matrix));
    }

    public boolean isValid() {
        synchronized(this.mThread.mSync) {
            this.sendEmptyMessage(3);

            try {
                this.mThread.mSync.wait();
            } catch (InterruptedException var4) {
                ;
            }

            return this.mThread.mSurface != null?this.mThread.mSurface.isValid():false;
        }
    }

    public final void release() {
        this.removeMessages(1);
        this.removeMessages(2);
        this.sendEmptyMessage(9);
    }

    public final void handleMessage(Message msg) {
        switch(msg.what) {
            case 1:
                RenderHandler.ContextParams params = (RenderHandler.ContextParams)msg.obj;
                this.mThread.handleSetEglContext(params.sharedContext, params.surface, msg.arg1 != 0);
                break;
            case 2:
                this.mThread.handleDraw(msg.arg1, (float[])((float[])msg.obj));
                break;
            case 3:
                synchronized(this.mThread.mSync) {
                    this.mThread.mSync.notify();
                    break;
                }
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            default:
                super.handleMessage(msg);
                break;
            case 9:
                Looper.myLooper().quit();
        }

    }

    private RenderHandler(RenderHandler.RenderThread thread) {
        this.mTexId = -1;
        this.mThread = thread;
    }

    private static final class RenderThread extends Thread {
        private static final String TAG_THREAD = "RenderThread";
        private final Object mSync = new Object();
        private RenderHandler mHandler;
        private EGLBase mEgl;
        private IEglSurface mTargetSurface;
        private Surface mSurface;
        private GLDrawer2D mDrawer;

        public RenderThread(String name) {
            super(name);
        }

        public final RenderHandler getHandler() {
            Object var1 = this.mSync;
            synchronized(this.mSync) {
                try {
                    this.mSync.wait();
                } catch (InterruptedException var4) {
                    ;
                }
            }

            return this.mHandler;
        }

        public final void handleSetEglContext(IContext shardContext, Object surface, boolean isRecordable) {
            this.release();
            Object e = this.mSync;
            synchronized(this.mSync) {
                this.mSurface = surface instanceof Surface?(Surface)surface:(surface instanceof SurfaceTexture?new Surface((SurfaceTexture)surface):null);
            }

            this.mEgl = EGLBase.createFrom(3, shardContext, false, 0, isRecordable);

            try {
                this.mTargetSurface = this.mEgl.createFromSurface(surface);
                this.mDrawer = new GLDrawer2D(isRecordable);
            } catch (Exception var7) {
                Log.w("RenderHandler", var7);
                if(this.mTargetSurface != null) {
                    this.mTargetSurface.release();
                    this.mTargetSurface = null;
                }

                if(this.mDrawer != null) {
                    this.mDrawer.release();
                    this.mDrawer = null;
                }
            }

        }

        public void handleDraw(int tex_id, float[] tex_matrix) {
            if(tex_id >= 0 && this.mTargetSurface != null) {
                this.mTargetSurface.makeCurrent();
                this.mDrawer.draw(tex_id, tex_matrix, 0);
                this.mTargetSurface.swap();
            }

        }

        public final void run() {
            Looper.prepare();
            Object var1 = this.mSync;
            synchronized(this.mSync) {
                this.mHandler = new RenderHandler(this);
                this.mSync.notify();
            }

            Looper.loop();
            this.release();
            var1 = this.mSync;
            synchronized(this.mSync) {
                this.mHandler = null;
            }
        }

        private final void release() {
            if(this.mDrawer != null) {
                this.mDrawer.release();
                this.mDrawer = null;
            }

            Object var1 = this.mSync;
            synchronized(this.mSync) {
                this.mSurface = null;
            }

            if(this.mTargetSurface != null) {
                this.clear();
                this.mTargetSurface.release();
                this.mTargetSurface = null;
            }

            if(this.mEgl != null) {
                this.mEgl.release();
                this.mEgl = null;
            }

        }

        private final void clear() {
            this.mTargetSurface.makeCurrent();
            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES20.glClear(16384);
            this.mTargetSurface.swap();
        }
    }

    private static final class ContextParams {
        final IContext sharedContext;
        final Object surface;

        public ContextParams(IContext sharedContext, Object surface) {
            this.sharedContext = sharedContext;
            this.surface = surface;
        }
    }
}
