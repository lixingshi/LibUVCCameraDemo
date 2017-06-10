package com.serenegiant.glutils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.Buffer;

/**
 * Created by john on 2017/6/10.
 */

public class TextureOffscreen {
    private static final boolean DEBUG = false;
    private static final String TAG = "TextureOffscreen";
    private static final boolean DEFAULT_ADJUST_POWER2 = false;
    private final int TEX_TARGET;
    private final boolean mHasDepthBuffer;
    private final boolean mAdjustPower2;
    private int mWidth;
    private int mHeight;
    private int mTexWidth;
    private int mTexHeight;
    private int mFBOTextureName;
    private int mDepthBufferObj;
    private int mFrameBufferObj;
    private final float[] mTexMatrix;
    private final float[] mResultMatrix;

    public TextureOffscreen(int width, int height) {
        this(3553, width, height, false, false);
    }

    public TextureOffscreen(int width, int height, boolean use_depth_buffer) {
        this(3553, width, height, use_depth_buffer, false);
    }

    public TextureOffscreen(int width, int height, boolean use_depth_buffer, boolean adjust_power2) {
        this(3553, width, height, use_depth_buffer, adjust_power2);
    }

    public TextureOffscreen(int tex_target, int width, int height, boolean use_depth_buffer, boolean adjust_power2) {
        this.mFBOTextureName = -1;
        this.mDepthBufferObj = -1;
        this.mFrameBufferObj = -1;
        this.mTexMatrix = new float[16];
        this.mResultMatrix = new float[16];
        this.TEX_TARGET = tex_target;
        this.mWidth = width;
        this.mHeight = height;
        this.mHasDepthBuffer = use_depth_buffer;
        this.mAdjustPower2 = adjust_power2;
        this.prepareFramebuffer(width, height);
    }

    public TextureOffscreen(int tex_id, int width, int height) {
        this(3553, tex_id, width, height, false, false);
    }

    public TextureOffscreen(int tex_id, int width, int height, boolean use_depth_buffer) {
        this(3553, tex_id, width, height, use_depth_buffer, false);
    }

    public TextureOffscreen(int tex_target, int tex_id, int width, int height, boolean use_depth_buffer, boolean adjust_power2) {
        this.mFBOTextureName = -1;
        this.mDepthBufferObj = -1;
        this.mFrameBufferObj = -1;
        this.mTexMatrix = new float[16];
        this.mResultMatrix = new float[16];
        this.TEX_TARGET = tex_target;
        this.mWidth = width;
        this.mHeight = height;
        this.mHasDepthBuffer = use_depth_buffer;
        this.mAdjustPower2 = adjust_power2;
        this.createFrameBuffer(width, height);
        this.assignTexture(tex_id, width, height);
    }

    public void release() {
        this.releaseFrameBuffer();
    }

    public void bind() {
        GLES20.glBindFramebuffer('赀', this.mFrameBufferObj);
        GLES20.glViewport(0, 0, this.mWidth, this.mHeight);
    }

    public void unbind() {
        GLES20.glBindFramebuffer('赀', 0);
    }

    public float[] getTexMatrix() {
        System.arraycopy(this.mTexMatrix, 0, this.mResultMatrix, 0, 16);
        return this.mResultMatrix;
    }

    public float[] getRawTexMatrix() {
        return this.mTexMatrix;
    }

    public void getTexMatrix(float[] matrix, int offset) {
        System.arraycopy(this.mTexMatrix, 0, matrix, offset, this.mTexMatrix.length);
    }

    public int getTexture() {
        return this.mFBOTextureName;
    }

    public void assignTexture(int texture_name, int width, int height) {
        if(width > this.mTexWidth || height > this.mTexHeight) {
            this.mWidth = width;
            this.mHeight = height;
            this.releaseFrameBuffer();
            this.createFrameBuffer(width, height);
        }

        this.mFBOTextureName = texture_name;
        GLES20.glBindFramebuffer('赀', this.mFrameBufferObj);
        GLHelper.checkGlError("glBindFramebuffer " + this.mFrameBufferObj);
        GLES20.glFramebufferTexture2D('赀', '賠', this.TEX_TARGET, this.mFBOTextureName, 0);
        GLHelper.checkGlError("glFramebufferTexture2D");
        if(this.mHasDepthBuffer) {
            GLES20.glFramebufferRenderbuffer('赀', '贀', '赁', this.mDepthBufferObj);
            GLHelper.checkGlError("glFramebufferRenderbuffer");
        }

        int status = GLES20.glCheckFramebufferStatus('赀');
        if(status != '賕') {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        } else {
            GLES20.glBindFramebuffer('赀', 0);
            Matrix.setIdentityM(this.mTexMatrix, 0);
            this.mTexMatrix[0] = (float)width / (float)this.mTexWidth;
            this.mTexMatrix[5] = (float)height / (float)this.mTexHeight;
        }
    }

    public void loadBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if(width > this.mTexWidth || height > this.mTexHeight) {
            this.mWidth = width;
            this.mHeight = height;
            this.releaseFrameBuffer();
            this.createFrameBuffer(width, height);
        }

        GLES20.glBindTexture(this.TEX_TARGET, this.mFBOTextureName);
        GLUtils.texImage2D(this.TEX_TARGET, 0, bitmap, 0);
        GLES20.glBindTexture(this.TEX_TARGET, 0);
        Matrix.setIdentityM(this.mTexMatrix, 0);
        this.mTexMatrix[0] = (float)width / (float)this.mTexWidth;
        this.mTexMatrix[5] = (float)height / (float)this.mTexHeight;
    }

    private final void prepareFramebuffer(int width, int height) {
        GLHelper.checkGlError("prepareFramebuffer start");
        this.createFrameBuffer(width, height);
        int tex_name = GLHelper.initTex(this.TEX_TARGET, '蓀', 9729, 9729, '脯');
        GLES20.glTexImage2D(this.TEX_TARGET, 0, 6408, this.mTexWidth, this.mTexHeight, 0, 6408, 5121, (Buffer)null);
        GLHelper.checkGlError("glTexImage2D");
        this.assignTexture(tex_name, width, height);
    }

    private final void createFrameBuffer(int width, int height) {
        int[] ids = new int[1];
        if(this.mAdjustPower2) {
            int w;
            for(w = 1; w < width; w <<= 1) {
                ;
            }

            int h;
            for(h = 1; h < height; h <<= 1) {
                ;
            }

            if(this.mTexWidth != w || this.mTexHeight != h) {
                this.mTexWidth = w;
                this.mTexHeight = h;
            }
        } else {
            this.mTexWidth = width;
            this.mTexHeight = height;
        }

        if(this.mHasDepthBuffer) {
            GLES20.glGenRenderbuffers(1, ids, 0);
            this.mDepthBufferObj = ids[0];
            GLES20.glBindRenderbuffer('赁', this.mDepthBufferObj);
            GLES20.glRenderbufferStorage('赁', '膥', this.mTexWidth, this.mTexHeight);
        }

        GLES20.glGenFramebuffers(1, ids, 0);
        GLHelper.checkGlError("glGenFramebuffers");
        this.mFrameBufferObj = ids[0];
        GLES20.glBindFramebuffer('赀', this.mFrameBufferObj);
        GLHelper.checkGlError("glBindFramebuffer " + this.mFrameBufferObj);
        GLES20.glBindFramebuffer('赀', 0);
    }

    private final void releaseFrameBuffer() {
        int[] names = new int[1];
        if(this.mDepthBufferObj >= 0) {
            names[0] = this.mDepthBufferObj;
            GLES20.glDeleteRenderbuffers(1, names, 0);
            this.mDepthBufferObj = 0;
        }

        if(this.mFBOTextureName >= 0) {
            names[0] = this.mFBOTextureName;
            GLES20.glDeleteTextures(1, names, 0);
            this.mFBOTextureName = -1;
        }

        if(this.mFrameBufferObj >= 0) {
            names[0] = this.mFrameBufferObj;
            GLES20.glDeleteFramebuffers(1, names, 0);
            this.mFrameBufferObj = -1;
        }

    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getTexWidth() {
        return this.mTexWidth;
    }

    public int getTexHeight() {
        return this.mTexHeight;
    }
}
