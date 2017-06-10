package com.serenegiant.glutils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.serenegiant.utils.BuildCheck;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by john on 2017/6/10.
 */

public class RendererHolder implements IRendererHolder {
    private static final String TAG = RendererHolder.class.getSimpleName();
    private final Object mSync = new Object();
    private final RenderHolderCallback mCallback;
    private volatile boolean isRunning;
    private File mCaptureFile;
    private final RendererHolder.RendererTask mRendererTask;
    private static final int REQUEST_DRAW = 1;
    private static final int REQUEST_UPDATE_SIZE = 2;
    private static final int REQUEST_ADD_SURFACE = 3;
    private static final int REQUEST_REMOVE_SURFACE = 4;
    private static final int REQUEST_RECREATE_MASTER_SURFACE = 5;
    private static final int REQUEST_MIRROR = 6;
    private final Runnable mCaptureTask = new Runnable() {
        EGLBase eglBase;
        EGLBase.IEglSurface captureSurface;
        GLDrawer2D drawer;

        public void run() {
            synchronized(RendererHolder.this.mSync) {
                if(!RendererHolder.this.isRunning) {
                    try {
                        RendererHolder.this.mSync.wait();
                    } catch (InterruptedException var4) {
                        ;
                    }
                }
            }

            this.init();
            if(this.eglBase.getGlVersion() > 2) {
                this.captureLoopGLES3();
            } else {
                this.captureLoopGLES2();
            }

            this.release();
        }

        private final void init() {
            this.eglBase = EGLBase.createFrom(3, RendererHolder.this.mRendererTask.getContext(), false, 0, false);
            this.captureSurface = this.eglBase.createOffscreen(RendererHolder.this.mRendererTask.mVideoWidth, RendererHolder.this.mRendererTask.mVideoHeight);
            this.drawer = new GLDrawer2D(true);
            float[] var10000 = this.drawer.getMvpMatrix();
            var10000[5] *= -1.0F;
        }

        private final void captureLoopGLES2() {
            int width = -1;
            int height = -1;
            ByteBuffer buf = null;
            File captureFile = null;

            while(RendererHolder.this.isRunning) {
                synchronized(RendererHolder.this.mSync) {
                    if(captureFile == null) {
                        if(RendererHolder.this.mCaptureFile == null) {
                            try {
                                RendererHolder.this.mSync.wait();
                            } catch (InterruptedException var16) {
                                break;
                            }
                        }

                        if(RendererHolder.this.mCaptureFile != null) {
                            captureFile = RendererHolder.this.mCaptureFile;
                            RendererHolder.this.mCaptureFile = null;
                        }
                    } else {
                        if(buf == null | width != RendererHolder.this.mRendererTask.mVideoWidth || height != RendererHolder.this.mRendererTask.mVideoHeight) {
                            width = RendererHolder.this.mRendererTask.mVideoWidth;
                            height = RendererHolder.this.mRendererTask.mVideoHeight;
                            buf = ByteBuffer.allocateDirect(width * height * 4);
                            buf.order(ByteOrder.LITTLE_ENDIAN);
                            if(this.captureSurface != null) {
                                this.captureSurface.release();
                                this.captureSurface = null;
                            }

                            this.captureSurface = this.eglBase.createOffscreen(width, height);
                        }

                        if(RendererHolder.this.isRunning) {
                            this.captureSurface.makeCurrent();
                            this.drawer.draw(RendererHolder.this.mRendererTask.mTexId, RendererHolder.this.mRendererTask.mTexMatrix, 0);
                            this.captureSurface.swap();
                            buf.clear();
                            GLES20.glReadPixels(0, 0, width, height, 6408, 5121, buf);
                            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
                            if(captureFile.toString().endsWith(".jpg")) {
                                compressFormat = Bitmap.CompressFormat.JPEG;
                            }

                            BufferedOutputStream os = null;

                            try {
                                try {
                                    os = new BufferedOutputStream(new FileOutputStream(captureFile));
                                    Bitmap e = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    buf.clear();
                                    e.copyPixelsFromBuffer(buf);
                                    e.compress(compressFormat, 90, os);
                                    e.recycle();
                                    os.flush();
                                } finally {
                                    if(os != null) {
                                        os.close();
                                    }

                                }
                            } catch (FileNotFoundException var18) {
                                Log.w(RendererHolder.TAG, "failed to save file", var18);
                            } catch (IOException var19) {
                                Log.w(RendererHolder.TAG, "failed to save file", var19);
                            }
                        }

                        captureFile = null;
                        RendererHolder.this.mSync.notifyAll();
                    }
                }
            }

        }

        private final void captureLoopGLES3() {
            int width = -1;
            int height = -1;
            ByteBuffer buf = null;
            File captureFile = null;

            while(RendererHolder.this.isRunning) {
                synchronized(RendererHolder.this.mSync) {
                    if(captureFile == null) {
                        if(RendererHolder.this.mCaptureFile == null) {
                            try {
                                RendererHolder.this.mSync.wait();
                            } catch (InterruptedException var16) {
                                break;
                            }
                        }

                        if(RendererHolder.this.mCaptureFile != null) {
                            captureFile = RendererHolder.this.mCaptureFile;
                            RendererHolder.this.mCaptureFile = null;
                        }
                    } else {
                        if(buf == null | width != RendererHolder.this.mRendererTask.mVideoWidth || height != RendererHolder.this.mRendererTask.mVideoHeight) {
                            width = RendererHolder.this.mRendererTask.mVideoWidth;
                            height = RendererHolder.this.mRendererTask.mVideoHeight;
                            buf = ByteBuffer.allocateDirect(width * height * 4);
                            buf.order(ByteOrder.LITTLE_ENDIAN);
                            if(this.captureSurface != null) {
                                this.captureSurface.release();
                                this.captureSurface = null;
                            }

                            this.captureSurface = this.eglBase.createOffscreen(width, height);
                        }

                        if(RendererHolder.this.isRunning) {
                            this.captureSurface.makeCurrent();
                            this.drawer.draw(RendererHolder.this.mRendererTask.mTexId, RendererHolder.this.mRendererTask.mTexMatrix, 0);
                            this.captureSurface.swap();
                            buf.clear();
                            GLES20.glReadPixels(0, 0, width, height, 6408, 5121, buf);
                            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
                            if(captureFile.toString().endsWith(".jpg")) {
                                compressFormat = Bitmap.CompressFormat.JPEG;
                            }

                            BufferedOutputStream os = null;

                            try {
                                try {
                                    os = new BufferedOutputStream(new FileOutputStream(captureFile));
                                    Bitmap e = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    buf.clear();
                                    e.copyPixelsFromBuffer(buf);
                                    e.compress(compressFormat, 90, os);
                                    e.recycle();
                                    os.flush();
                                } finally {
                                    if(os != null) {
                                        os.close();
                                    }

                                }
                            } catch (FileNotFoundException var18) {
                                Log.w(RendererHolder.TAG, "failed to save file", var18);
                            } catch (IOException var19) {
                                Log.w(RendererHolder.TAG, "failed to save file", var19);
                            }
                        }

                        captureFile = null;
                        RendererHolder.this.mSync.notifyAll();
                    }
                }
            }

        }

        private final void release() {
            if(this.captureSurface != null) {
                this.captureSurface.makeCurrent();
                if(this.drawer != null) {
                    this.drawer.release();
                }

                this.captureSurface.release();
                this.captureSurface = null;
            }

            if(this.drawer != null) {
                this.drawer.release();
                this.drawer = null;
            }

            if(this.eglBase != null) {
                this.eglBase.release();
                this.eglBase = null;
            }

        }
    };

    public RendererHolder(int width, int height, @Nullable RenderHolderCallback callback) {
        this.mCallback = callback;
        this.mRendererTask = new RendererHolder.RendererTask(this, width, height);
        (new Thread(this.mRendererTask, TAG)).start();
        if(!this.mRendererTask.waitReady()) {
            throw new RuntimeException("failed to start renderer thread");
        } else {
            (new Thread(this.mCaptureTask, "CaptureTask")).start();
            Object var4 = this.mSync;
            synchronized(this.mSync) {
                if(!this.isRunning) {
                    try {
                        this.mSync.wait();
                    } catch (InterruptedException var7) {
                        ;
                    }
                }

            }
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void release() {
        this.mRendererTask.release();
        Object var1 = this.mSync;
        synchronized(this.mSync) {
            this.isRunning = false;
            this.mSync.notifyAll();
        }
    }

    public Surface getSurface() {
        return this.mRendererTask.getSurface();
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mRendererTask.getSurfaceTexture();
    }

    public void reset() {
        this.mRendererTask.checkMasterSurface();
    }

    public void resize(int width, int height) {
        this.mRendererTask.resize(width, height);
    }

    public void setMirror(int mirror) {
        this.mRendererTask.mirror(mirror % 4);
    }

    public int getMirror() {
        return this.mRendererTask.mirror();
    }

    public void addSurface(int id, Object surface, boolean isRecordable) {
        this.mRendererTask.addSurface(id, surface);
    }

    public void addSurface(int id, Object surface, boolean isRecordable, int maxFps) {
        this.mRendererTask.addSurface(id, surface, maxFps);
    }

    public void removeSurface(int id) {
        this.mRendererTask.removeSurface(id);
    }

    public boolean isEnabled(int id) {
        return this.mRendererTask.isEnabled(id);
    }

    public void setEnabled(int id, boolean enable) {
        this.mRendererTask.setEnabled(id, enable);
    }

    public void requestFrame() {
        this.mRendererTask.removeRequest(1);
        this.mRendererTask.offer(1);
    }

    public int getCount() {
        return this.mRendererTask.getCount();
    }

    public void captureStillAsync(String path) {
        File file = new File(path);
        Object var3 = this.mSync;
        synchronized(this.mSync) {
            this.mCaptureFile = file;
            this.mSync.notifyAll();
        }
    }

    public void captureStill(String path) {
        File file = new File(path);
        Object var3 = this.mSync;
        synchronized(this.mSync) {
            this.mCaptureFile = file;
            this.mSync.notifyAll();

            try {
                this.mSync.wait();
            } catch (InterruptedException var6) {
                ;
            }

        }
    }

    private static final class RendererTask extends EglTask {
        private final Object mClientSync = new Object();
        private final SparseArray<RendererSurfaceRec> mClients = new SparseArray();
        private final RendererHolder mParent;
        private GLDrawer2D mDrawer;
        private int mTexId;
        private SurfaceTexture mMasterTexture;
        final float[] mTexMatrix = new float[16];
        private Surface mMasterSurface;
        private int mVideoWidth;
        private int mVideoHeight;
        private int mMirror = 0;
        private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                RendererTask.this.offer(1);
            }
        };

        public RendererTask(RendererHolder parent, int width, int height) {
            super(3, (EGLBase.IContext)null, 2);
            this.mParent = parent;
            this.mVideoWidth = width;
            this.mVideoHeight = height;
        }

        protected void onStart() {
            this.mDrawer = new GLDrawer2D(true);
            this.handleReCreateMasterSurface();
            synchronized(this.mParent.mSync) {
                this.mParent.isRunning = true;
                this.mParent.mSync.notifyAll();
            }
        }

        protected void onStop() {
            synchronized(this.mParent.mSync) {
                this.mParent.isRunning = false;
                this.mParent.mSync.notifyAll();
            }

            this.makeCurrent();
            if(this.mDrawer != null) {
                this.mDrawer.release();
                this.mDrawer = null;
            }

            this.handleReleaseMasterSurface();
            this.handleRemoveAll();
        }

        protected boolean onError(Exception e) {
            return false;
        }

        protected Object processRequest(int request, int arg1, int arg2, Object obj) {
            switch(request) {
                case 1:
                    this.handleDraw();
                    break;
                case 2:
                    this.handleResize(arg1, arg2);
                    break;
                case 3:
                    this.handleAddSurface(arg1, obj, arg2);
                    break;
                case 4:
                    this.handleRemoveSurface(arg1);
                    break;
                case 5:
                    this.handleReCreateMasterSurface();
                    break;
                case 6:
                    this.handleMirror(arg1);
            }

            return null;
        }

        public Surface getSurface() {
            this.checkMasterSurface();
            return this.mMasterSurface;
        }

        public SurfaceTexture getSurfaceTexture() {
            this.checkMasterSurface();
            return this.mMasterTexture;
        }

        public void addSurface(int id, Object surface) {
            this.addSurface(id, surface, -1);
        }

        public void addSurface(int id, Object surface, int maxFps) {
            this.checkFinished();
            if(!(surface instanceof SurfaceTexture) && !(surface instanceof Surface) && !(surface instanceof SurfaceHolder)) {
                throw new IllegalArgumentException("Surface should be one of Surface, SurfaceTexture or SurfaceHolder");
            } else {
                Object var4 = this.mClientSync;
                synchronized(this.mClientSync) {
                    if(this.mClients.get(id) == null) {
                        while(this.isRunning()) {
                            if(this.offer(3, id, maxFps, surface)) {
                                try {
                                    this.mClientSync.wait();
                                } catch (InterruptedException var7) {
                                    ;
                                }
                                break;
                            }

                            try {
                                this.mClientSync.wait(10L);
                            } catch (InterruptedException var8) {
                                break;
                            }
                        }
                    }

                }
            }
        }

        public void removeSurface(int id) {
            Object var2 = this.mClientSync;
            synchronized(this.mClientSync) {
                if(this.mClients.get(id) != null) {
                    while(this.isRunning()) {
                        if(this.offer(4, id)) {
                            try {
                                this.mClientSync.wait();
                            } catch (InterruptedException var5) {
                                ;
                            }
                            break;
                        }

                        try {
                            this.mClientSync.wait(10L);
                        } catch (InterruptedException var6) {
                            break;
                        }
                    }
                }

            }
        }

        public boolean isEnabled(int id) {
            Object var2 = this.mClientSync;
            synchronized(this.mClientSync) {
                RendererSurfaceRec rec = (RendererSurfaceRec)this.mClients.get(id);
                return rec != null && rec.isEnabled();
            }
        }

        public void setEnabled(int id, boolean enable) {
            Object var3 = this.mClientSync;
            synchronized(this.mClientSync) {
                RendererSurfaceRec rec = (RendererSurfaceRec)this.mClients.get(id);
                if(rec != null) {
                    rec.setEnabled(enable);
                }

            }
        }

        public int getCount() {
            Object var1 = this.mClientSync;
            synchronized(this.mClientSync) {
                return this.mClients.size();
            }
        }

        public void resize(int width, int height) {
            this.checkFinished();
            if(this.mVideoWidth != width || this.mVideoHeight != height) {
                this.offer(2, width, height);
            }

        }

        public void mirror(int mirror) {
            this.checkFinished();
            if(this.mMirror != mirror) {
                this.offer(6, mirror);
            }

        }

        public int mirror() {
            return this.mMirror;
        }

        public void checkMasterSurface() {
            this.checkFinished();
            if(this.mMasterSurface == null || !this.mMasterSurface.isValid()) {
                Log.d(RendererHolder.TAG, "checkMasterSurface:invalid master surface");
                this.offerAndWait(5, 0, 0, (Object)null);
            }

        }

        private void checkFinished() {
            if(this.isFinished()) {
                throw new RuntimeException("already finished");
            }
        }

        private void handleDraw() {
            if(this.mMasterSurface != null && this.mMasterSurface.isValid()) {
                try {
                    this.makeCurrent();
                    this.mMasterTexture.updateTexImage();
                    this.mMasterTexture.getTransformMatrix(this.mTexMatrix);
                } catch (Exception var10) {
                    Log.w(RendererHolder.TAG, "draw:thread id =" + Thread.currentThread().getId(), var10);
                    this.offer(5);
                    return;
                }

                synchronized(this.mParent.mCaptureTask) {
                    this.mParent.mCaptureTask.notify();
                }

                Object e = this.mClientSync;
                synchronized(this.mClientSync) {
                    int n = this.mClients.size();

                    for(int i = n - 1; i >= 0; --i) {
                        RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.valueAt(i);
                        if(client != null && client.canDraw()) {
                            try {
                                client.draw(this.mDrawer, this.mTexId, this.mTexMatrix);
                            } catch (Exception var8) {
                                this.mClients.removeAt(i);
                                client.release();
                            }
                        }
                    }
                }

                if(this.mParent.mCallback != null) {
                    try {
                        this.mParent.mCallback.onFrameAvailable();
                    } catch (Exception var7) {
                        ;
                    }
                }

                GLES20.glClear(16384);
                GLES20.glFlush();
            } else {
                Log.w(RendererHolder.TAG, "checkMasterSurface:invalid master surface");
                this.offer(5);
            }
        }

        private void handleAddSurface(int id, Object surface, int maxFps) {
            this.checkSurface();
            Object var4 = this.mClientSync;
            synchronized(this.mClientSync) {
                RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.get(id);
                if(client == null) {
                    try {
                        client = RendererSurfaceRec.newInstance(this.getEgl(), surface, maxFps);
                        this.setMirror(client, this.mMirror);
                        this.mClients.append(id, client);
                    } catch (Exception var8) {
                        Log.w(RendererHolder.TAG, "invalid surface: surface=" + surface, var8);
                    }
                } else {
                    Log.w(RendererHolder.TAG, "surface is already added: id=" + id);
                }

                this.mClientSync.notifyAll();
            }
        }

        private void handleRemoveSurface(int id) {
            Object var2 = this.mClientSync;
            synchronized(this.mClientSync) {
                RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.get(id);
                if(client != null) {
                    this.mClients.remove(id);
                    client.release();
                }

                this.checkSurface();
                this.mClientSync.notifyAll();
            }
        }

        private void handleRemoveAll() {
            Object var1 = this.mClientSync;
            synchronized(this.mClientSync) {
                int n = this.mClients.size();

                for(int i = 0; i < n; ++i) {
                    RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.valueAt(i);
                    if(client != null) {
                        this.makeCurrent();
                        client.release();
                    }
                }

                this.mClients.clear();
            }
        }

        private void checkSurface() {
            Object var1 = this.mClientSync;
            synchronized(this.mClientSync) {
                int n = this.mClients.size();

                for(int i = 0; i < n; ++i) {
                    RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.valueAt(i);
                    if(client != null && !client.isValid()) {
                        int id = this.mClients.keyAt(i);
                        ((RendererSurfaceRec)this.mClients.valueAt(i)).release();
                        this.mClients.remove(id);
                    }
                }

            }
        }

        @SuppressLint({"NewApi"})
        private void handleReCreateMasterSurface() {
            this.makeCurrent();
            this.handleReleaseMasterSurface();
            this.makeCurrent();
            this.mTexId = GLHelper.initTex('赥', 9728);
            this.mMasterTexture = new SurfaceTexture(this.mTexId);
            this.mMasterSurface = new Surface(this.mMasterTexture);
            if(BuildCheck.isAndroid4_1()) {
                this.mMasterTexture.setDefaultBufferSize(this.mVideoWidth, this.mVideoHeight);
            }

            this.mMasterTexture.setOnFrameAvailableListener(this.mOnFrameAvailableListener);

            try {
                if(this.mParent.mCallback != null) {
                    this.mParent.mCallback.onCreate(this.mMasterSurface);
                }
            } catch (Exception var2) {
                Log.w(RendererHolder.TAG, var2);
            }

        }

        private void handleReleaseMasterSurface() {
            try {
                if(this.mParent.mCallback != null) {
                    this.mParent.mCallback.onDestroy();
                }
            } catch (Exception var2) {
                Log.w(RendererHolder.TAG, var2);
            }

            this.mMasterSurface = null;
            if(this.mMasterTexture != null) {
                this.mMasterTexture.release();
                this.mMasterTexture = null;
            }

            if(this.mTexId != 0) {
                GLHelper.deleteTex(this.mTexId);
                this.mTexId = 0;
            }

        }

        @SuppressLint({"NewApi"})
        private void handleResize(int width, int height) {
            this.mVideoWidth = width;
            this.mVideoHeight = height;
            if(BuildCheck.isAndroid4_1()) {
                this.mMasterTexture.setDefaultBufferSize(this.mVideoWidth, this.mVideoHeight);
            }

        }

        private void handleMirror(int mirror) {
            this.mMirror = mirror;
            Object var2 = this.mClientSync;
            synchronized(this.mClientSync) {
                int n = this.mClients.size();

                for(int i = 0; i < n; ++i) {
                    RendererSurfaceRec client = (RendererSurfaceRec)this.mClients.valueAt(i);
                    if(client != null) {
                        this.setMirror(client, mirror);
                    }
                }

            }
        }

        private void setMirror(RendererSurfaceRec client, int mirror) {
            float[] mvp = client.mMvpMatrix;
            switch(mirror) {
                case 0:
                    mvp[0] = Math.abs(mvp[0]);
                    mvp[5] = Math.abs(mvp[5]);
                    break;
                case 1:
                    mvp[0] = -Math.abs(mvp[0]);
                    mvp[5] = Math.abs(mvp[5]);
                    break;
                case 2:
                    mvp[0] = Math.abs(mvp[0]);
                    mvp[5] = -Math.abs(mvp[5]);
                    break;
                case 3:
                    mvp[0] = -Math.abs(mvp[0]);
                    mvp[5] = -Math.abs(mvp[5]);
            }

        }
    }
}
