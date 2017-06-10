package com.serenegiant.glutils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.serenegiant.utils.BuildCheck;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by john on 2017/6/10.
 */

public class EGLBase10 extends EGLBase {
    private static final String TAG = "EGLBase10";
    private EGL10 mEgl = null;
    private EGLDisplay mEglDisplay = null;
    private EGLBase10.Config mEglConfig = null;
    private int mGlVersion = 2;
    private static final EGLBase10.Context EGL_NO_CONTEXT;
    @NonNull
    private EGLBase10.Context mContext;

    public EGLBase10(int maxClientVersion, EGLBase10.Context sharedContext, boolean withDepthBuffer, int stencilBits, boolean isRecordable) {
        this.mContext = EGL_NO_CONTEXT;
        this.init(maxClientVersion, sharedContext, withDepthBuffer, stencilBits, isRecordable);
    }

    public void release() {
        this.destroyContext();
        this.mContext = EGL_NO_CONTEXT;
        if(this.mEgl != null) {
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEgl.eglTerminate(this.mEglDisplay);
            this.mEglDisplay = null;
            this.mEglConfig = null;
            this.mEgl = null;
        }
    }

    public EGLBase10.EglSurface createFromSurface(Object nativeWindow) {
        EGLBase10.EglSurface eglSurface = new EGLBase10.EglSurface(this, nativeWindow);
        eglSurface.makeCurrent();
        return eglSurface;
    }

    public EGLBase10.EglSurface createOffscreen(int width, int height) {
        EGLBase10.EglSurface eglSurface = new EGLBase10.EglSurface(this, width, height);
        eglSurface.makeCurrent();
        return eglSurface;
    }

    public EGLBase10.Context getContext() {
        return this.mContext;
    }

    public EGLBase10.Config getConfig() {
        return this.mEglConfig;
    }

    public void makeDefault() {
        if(!this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
            Log.w("EGLBase10", "makeDefault:eglMakeCurrent:err=" + this.mEgl.eglGetError());
        }

    }

    public void sync() {
        this.mEgl.eglWaitGL();
        this.mEgl.eglWaitNative(12379, (Object)null);
    }

    public String queryString(int what) {
        return this.mEgl.eglQueryString(this.mEglDisplay, what);
    }

    public int getGlVersion() {
        return this.mGlVersion;
    }

    private final void init(int maxClientVersion, @Nullable EGLBase10.Context sharedContext, boolean withDepthBuffer, int stencilBits, boolean isRecordable) {
        sharedContext = sharedContext != null?sharedContext:EGL_NO_CONTEXT;
        if(this.mEgl == null) {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if(this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            int[] config = new int[2];
            if(!this.mEgl.eglInitialize(this.mEglDisplay, config)) {
                this.mEglDisplay = null;
                throw new RuntimeException("eglInitialize failed");
            }
        }

        EGLContext values;
        EGLConfig config1;
        if(maxClientVersion >= 3) {
            config1 = this.getConfig(3, withDepthBuffer, stencilBits, isRecordable);
            if(config1 != null) {
                values = this.createContext(sharedContext, config1, 3);
                if(this.mEgl.eglGetError() == 12288) {
                    this.mEglConfig = new EGLBase10.Config(config1);
                    this.mContext = new EGLBase10.Context(values);
                    this.mGlVersion = 3;
                }
            }
        }

        if(maxClientVersion >= 2 && (this.mContext == null || this.mContext.eglContext == EGL10.EGL_NO_CONTEXT)) {
            config1 = this.getConfig(2, withDepthBuffer, stencilBits, isRecordable);
            if(config1 == null) {
                throw new RuntimeException("chooseConfig failed");
            }

            try {
                values = this.createContext(sharedContext, config1, 2);
                this.checkEglError("eglCreateContext");
                this.mEglConfig = new EGLBase10.Config(config1);
                this.mContext = new EGLBase10.Context(values);
                this.mGlVersion = 2;
            } catch (Exception var9) {
                if(isRecordable) {
                    config1 = this.getConfig(2, withDepthBuffer, stencilBits, false);
                    if(config1 == null) {
                        throw new RuntimeException("chooseConfig failed");
                    }

                    EGLContext context = this.createContext(sharedContext, config1, 2);
                    this.checkEglError("eglCreateContext");
                    this.mEglConfig = new EGLBase10.Config(config1);
                    this.mContext = new EGLBase10.Context(context);
                    this.mGlVersion = 2;
                }
            }
        }

        if(this.mContext == null || this.mContext.eglContext == EGL10.EGL_NO_CONTEXT) {
            config1 = this.getConfig(1, withDepthBuffer, stencilBits, isRecordable);
            if(config1 == null) {
                throw new RuntimeException("chooseConfig failed");
            }

            values = this.createContext(sharedContext, config1, 1);
            this.checkEglError("eglCreateContext");
            this.mEglConfig = new EGLBase10.Config(config1);
            this.mContext = new EGLBase10.Context(values);
            this.mGlVersion = 1;
        }

        int[] values1 = new int[1];
        this.mEgl.eglQueryContext(this.mEglDisplay, this.mContext.eglContext, 12440, values1);
        Log.d("EGLBase10", "EGLContext created, client version " + values1[0]);
        this.makeDefault();
    }

    private final boolean makeCurrent(EGLSurface surface) {
        if(surface != null && surface != EGL10.EGL_NO_SURFACE) {
            if(!this.mEgl.eglMakeCurrent(this.mEglDisplay, surface, surface, this.mContext.eglContext)) {
                Log.w("TAG", "eglMakeCurrent" + this.mEgl.eglGetError());
                return false;
            } else {
                return true;
            }
        } else {
            int error = this.mEgl.eglGetError();
            if(error == 12299) {
                Log.e("EGLBase10", "makeCurrent:EGL_BAD_NATIVE_WINDOW");
            }

            return false;
        }
    }

    private final int swap(EGLSurface surface) {
        if(!this.mEgl.eglSwapBuffers(this.mEglDisplay, surface)) {
            int err = this.mEgl.eglGetError();
            return err;
        } else {
            return 12288;
        }
    }

    private final int swap(EGLSurface surface, long ignored) {
        if(!this.mEgl.eglSwapBuffers(this.mEglDisplay, surface)) {
            int err = this.mEgl.eglGetError();
            return err;
        } else {
            return 12288;
        }
    }

    private final EGLContext createContext(@NonNull EGLBase10.Context sharedContext, EGLConfig config, int version) {
        int[] attrib_list = new int[]{12440, version, 12344};
        EGLContext context = this.mEgl.eglCreateContext(this.mEglDisplay, config, sharedContext.eglContext, attrib_list);
        return context;
    }

    private final void destroyContext() {
        if(!this.mEgl.eglDestroyContext(this.mEglDisplay, this.mContext.eglContext)) {
            Log.e("destroyContext", "display:" + this.mEglDisplay + " context: " + this.mContext.eglContext);
            Log.e("EGLBase10", "eglDestroyContext:" + this.mEgl.eglGetError());
        }

        this.mContext = EGL_NO_CONTEXT;
    }

    private final int getSurfaceWidth(EGLSurface surface) {
        int[] value = new int[1];
        boolean ret = this.mEgl.eglQuerySurface(this.mEglDisplay, surface, 12375, value);
        if(!ret) {
            value[0] = 0;
        }

        return value[0];
    }

    private final int getSurfaceHeight(EGLSurface surface) {
        int[] value = new int[1];
        boolean ret = this.mEgl.eglQuerySurface(this.mEglDisplay, surface, 12374, value);
        if(!ret) {
            value[0] = 0;
        }

        return value[0];
    }

    private final EGLSurface createWindowSurface(Object nativeWindow) {
        int[] surfaceAttribs = new int[]{12344};
        EGLSurface result = null;

        try {
            result = this.mEgl.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig.eglConfig, nativeWindow, surfaceAttribs);
            if(result != null && result != EGL10.EGL_NO_SURFACE) {
                this.makeCurrent(result);
                return result;
            } else {
                int e = this.mEgl.eglGetError();
                if(e == 12299) {
                    Log.e("EGLBase10", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                }

                throw new RuntimeException("createWindowSurface failed error=" + e);
            }
        } catch (Exception var5) {
            Log.e("EGLBase10", "eglCreateWindowSurface", var5);
            throw new IllegalArgumentException(var5);
        }
    }

    private final EGLSurface createOffscreenSurface(int width, int height) {
        int[] surfaceAttribs = new int[]{12375, width, 12374, height, 12344};
        this.mEgl.eglWaitGL();
        EGLSurface result = null;

        try {
            result = this.mEgl.eglCreatePbufferSurface(this.mEglDisplay, this.mEglConfig.eglConfig, surfaceAttribs);
            this.checkEglError("eglCreatePbufferSurface");
            if(result == null) {
                throw new RuntimeException("surface was null");
            }
        } catch (IllegalArgumentException var6) {
            Log.e("EGLBase10", "createOffscreenSurface", var6);
        } catch (RuntimeException var7) {
            Log.e("EGLBase10", "createOffscreenSurface", var7);
        }

        return result;
    }

    private final void destroyWindowSurface(EGLSurface surface) {
        if(surface != EGL10.EGL_NO_SURFACE) {
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEgl.eglDestroySurface(this.mEglDisplay, surface);
        }

        surface = EGL10.EGL_NO_SURFACE;
    }

    private final void checkEglError(String msg) {
        int error;
        if((error = this.mEgl.eglGetError()) != 12288) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    private final EGLConfig getConfig(int version, boolean hasDepthBuffer, int stencilBits, boolean isRecordable) {
        int renderableType = 4;
        if(version >= 3) {
            renderableType |= 64;
        }

        int[] attribList = new int[]{12352, renderableType, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12344, 12344, 12344, 12344, 12344, 12344, 12344};
        int offset = 10;
        if(stencilBits > 0) {
            attribList[offset++] = 12326;
            attribList[offset++] = 8;
        }

        if(hasDepthBuffer) {
            attribList[offset++] = 12325;
            attribList[offset++] = 16;
        }

        if(isRecordable && BuildCheck.isAndroid4_3()) {
            attribList[offset++] = 12610;
            attribList[offset++] = 1;
        }

        for(int config = attribList.length - 1; config >= offset; --config) {
            attribList[config] = 12344;
        }

        EGLConfig var12 = this.internalGetConfig(attribList);
        if(var12 == null && version == 2 && isRecordable) {
            int n = attribList.length;

            label51:
            for(int i = 10; i < n - 1; i += 2) {
                if(attribList[i] == 12610) {
                    int j = i;

                    while(true) {
                        if(j >= n) {
                            break label51;
                        }

                        attribList[j] = 12344;
                        ++j;
                    }
                }
            }

            var12 = this.internalGetConfig(attribList);
        }

        if(var12 == null) {
            Log.w("EGLBase10", "try to fallback to RGB565");
            attribList[3] = 5;
            attribList[5] = 6;
            attribList[7] = 5;
            var12 = this.internalGetConfig(attribList);
        }

        return var12;
    }

    private EGLConfig internalGetConfig(int[] attribList) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        return !this.mEgl.eglChooseConfig(this.mEglDisplay, attribList, configs, configs.length, numConfigs)?null:configs[0];
    }

    static {
        EGL_NO_CONTEXT = new EGLBase10.Context(EGL10.EGL_NO_CONTEXT);
    }

    public static class EglSurface implements IEglSurface {
        private final EGLBase10 mEglBase;
        private EGLSurface mEglSurface;

        private EglSurface(EGLBase10 eglBase, Object surface) throws IllegalArgumentException {
            this.mEglSurface = EGL10.EGL_NO_SURFACE;
            this.mEglBase = eglBase;
            if(surface instanceof Surface && !BuildCheck.isAndroid4_2()) {
                this.mEglSurface = this.mEglBase.createWindowSurface(new EGLBase10.MySurfaceHolder((Surface)surface));
            } else {
                if(!(surface instanceof Surface) && !(surface instanceof SurfaceHolder) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceView)) {
                    throw new IllegalArgumentException("unsupported surface");
                }

                this.mEglSurface = this.mEglBase.createWindowSurface(surface);
            }

        }

        private EglSurface(EGLBase10 eglBase, int width, int height) {
            this.mEglSurface = EGL10.EGL_NO_SURFACE;
            this.mEglBase = eglBase;
            if(width > 0 && height > 0) {
                this.mEglSurface = this.mEglBase.createOffscreenSurface(width, height);
            } else {
                this.mEglSurface = this.mEglBase.createOffscreenSurface(1, 1);
            }

        }

        public void makeCurrent() {
            this.mEglBase.makeCurrent(this.mEglSurface);
            if(this.mEglBase.getGlVersion() >= 2) {
                GLES20.glViewport(0, 0, this.mEglBase.getSurfaceWidth(this.mEglSurface), this.mEglBase.getSurfaceHeight(this.mEglSurface));
            } else {
                GLES10.glViewport(0, 0, this.mEglBase.getSurfaceWidth(this.mEglSurface), this.mEglBase.getSurfaceHeight(this.mEglSurface));
            }

        }

        public void swap() {
            this.mEglBase.swap(this.mEglSurface);
        }

        public void swap(long presentationTimeNs) {
            this.mEglBase.swap(this.mEglSurface, presentationTimeNs);
        }

        public IContext getContext() {
            return this.mEglBase.getContext();
        }

        public void setPresentationTime(long presentationTimeNs) {
        }

        public boolean isValid() {
            return this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE && this.mEglBase.getSurfaceWidth(this.mEglSurface) > 0 && this.mEglBase.getSurfaceHeight(this.mEglSurface) > 0;
        }

        public void release() {
            this.mEglBase.makeDefault();
            this.mEglBase.destroyWindowSurface(this.mEglSurface);
            this.mEglSurface = EGL10.EGL_NO_SURFACE;
        }
    }

    public static class MySurfaceHolder implements SurfaceHolder {
        private final Surface surface;

        public MySurfaceHolder(Surface surface) {
            this.surface = surface;
        }

        public Surface getSurface() {
            return this.surface;
        }

        public void addCallback(Callback callback) {
        }

        public void removeCallback(Callback callback) {
        }

        public boolean isCreating() {
            return false;
        }

        public void setType(int type) {
        }

        public void setFixedSize(int width, int height) {
        }

        public void setSizeFromLayout() {
        }

        public void setFormat(int format) {
        }

        public void setKeepScreenOn(boolean screenOn) {
        }

        public Canvas lockCanvas() {
            return null;
        }

        public Canvas lockCanvas(Rect dirty) {
            return null;
        }

        public void unlockCanvasAndPost(Canvas canvas) {
        }

        public Rect getSurfaceFrame() {
            return null;
        }
    }

    public static class Config extends IConfig {
        public final EGLConfig eglConfig;

        private Config(EGLConfig eglConfig) {
            this.eglConfig = eglConfig;
        }
    }

    public static class Context extends IContext {
        public final EGLContext eglContext;

        private Context(EGLContext context) {
            this.eglContext = context;
        }
    }
}
