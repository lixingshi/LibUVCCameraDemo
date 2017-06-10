package com.serenegiant.glutils;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by john on 2017/6/10.
 */
public class GLDrawer2D implements IDrawer2dES2 {
    private static final float[] VERTICES = new float[]{1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F};
    private static final float[] TEXCOORD = new float[]{1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F};
    private static final int FLOAT_SZ = 4;
    private final int VERTEX_NUM;
    private final int VERTEX_SZ;
    private final FloatBuffer pVertex;
    private final FloatBuffer pTexCoord;
    private final int mTexTarget;
    private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    private final float[] mMvpMatrix;

    public GLDrawer2D(boolean isOES) {
        this(VERTICES, TEXCOORD, isOES);
    }

    public GLDrawer2D(float[] vertices, float[] texcoord, boolean isOES) {
        this.mMvpMatrix = new float[16];
        this.VERTEX_NUM = Math.min(vertices != null?vertices.length:0, texcoord != null?texcoord.length:0) / 2;
        this.VERTEX_SZ = this.VERTEX_NUM * 2;
        this.mTexTarget = isOES?'赥':3553;
        this.pVertex = ByteBuffer.allocateDirect(this.VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pVertex.put(vertices);
        this.pVertex.flip();
        this.pTexCoord = ByteBuffer.allocateDirect(this.VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(texcoord);
        this.pTexCoord.flip();
        if(isOES) {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\nprecision mediump float;\nuniform sampler2D sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        }

        Matrix.setIdentityM(this.mMvpMatrix, 0);
        this.init();
    }

    public void release() {
        if(this.hProgram >= 0) {
            GLES20.glDeleteProgram(this.hProgram);
        }

        this.hProgram = -1;
    }

    public boolean isOES() {
        return this.mTexTarget == '赥';
    }

    public float[] getMvpMatrix() {
        return this.mMvpMatrix;
    }

    public IDrawer2D setMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(matrix, offset, this.mMvpMatrix, 0, 16);
        return this;
    }

    public void getMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(this.mMvpMatrix, 0, matrix, offset, 16);
    }

    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        if(this.hProgram >= 0) {
            GLES20.glUseProgram(this.hProgram);
            if(tex_matrix != null) {
                GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, offset);
            }

            GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
            GLES20.glActiveTexture('蓀');
            GLES20.glBindTexture(this.mTexTarget, texId);
            GLES20.glDrawArrays(5, 0, this.VERTEX_NUM);
            GLES20.glBindTexture(this.mTexTarget, 0);
            GLES20.glUseProgram(0);
        }
    }

    public void draw(ITexture texture) {
        this.draw(texture.getTexture(), texture.getTexMatrix(), 0);
    }

    public void draw(TextureOffscreen offscreen) {
        this.draw(offscreen.getTexture(), offscreen.getTexMatrix(), 0);
    }

    public int initTex() {
        return GLHelper.initTex(this.mTexTarget, 9728);
    }

    public void deleteTex(int hTex) {
        GLHelper.deleteTex(hTex);
    }

    public synchronized void updateShader(String vs, String fs) {
        this.release();
        this.hProgram = GLHelper.loadShader(vs, fs);
        this.init();
    }

    public void updateShader(String fs) {
        this.updateShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", fs);
    }

    public void resetShader() {
        this.release();
        if(this.isOES()) {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\nprecision mediump float;\nuniform sampler2D sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        }

        this.init();
    }

    public int glGetAttribLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetAttribLocation(this.hProgram, name);
    }

    public int glGetUniformLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetUniformLocation(this.hProgram, name);
    }

    public void glUseProgram() {
        GLES20.glUseProgram(this.hProgram);
    }

    private void init() {
        GLES20.glUseProgram(this.hProgram);
        this.maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "aPosition");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, this.mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, 5126, false, this.VERTEX_SZ, this.pVertex);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, 5126, false, this.VERTEX_SZ, this.pTexCoord);
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLoc);
    }
}
