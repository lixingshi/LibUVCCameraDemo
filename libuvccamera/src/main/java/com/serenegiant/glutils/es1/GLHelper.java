package com.serenegiant.glutils.es1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES10;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.serenegiant.utils.BuildCheck;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by john on 2017/6/10.
 */

public final class GLHelper {
    private static final String TAG = "GLHelper";
    private static final float[] sScratch = new float[32];

    public GLHelper() {
    }

    public static void checkGlError(String op) {
        int error = GLES10.glGetError();
        if(error != 0) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("GLHelper", msg);
            (new Throwable(msg)).printStackTrace();
        }

    }

    public static void checkGlError(GL10 gl, String op) {
        int error = gl.glGetError();
        if(error != 0) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("GLHelper", msg);
            (new Throwable(msg)).printStackTrace();
        }

    }

    public static int initTex(int texTarget, int filter_param) {
        return initTex(texTarget, '蓀', filter_param, filter_param, '脯');
    }

    public static int initTex(int texTarget, int texUnit, int min_filter, int mag_filter, int wrap) {
        int[] tex = new int[1];
        GLES10.glActiveTexture(texUnit);
        GLES10.glGenTextures(1, tex, 0);
        GLES10.glBindTexture(texTarget, tex[0]);
        GLES10.glTexParameterx(texTarget, 10242, wrap);
        GLES10.glTexParameterx(texTarget, 10243, wrap);
        GLES10.glTexParameterx(texTarget, 10241, min_filter);
        GLES10.glTexParameterx(texTarget, 10240, mag_filter);
        return tex[0];
    }

    public static int initTex(GL10 gl, int texTarget, int filter_param) {
        int[] tex = new int[1];
        gl.glActiveTexture('蓀');
        gl.glGenTextures(1, tex, 0);
        gl.glBindTexture(texTarget, tex[0]);
        gl.glTexParameterx(texTarget, 10242, '脯');
        gl.glTexParameterx(texTarget, 10243, '脯');
        gl.glTexParameterx(texTarget, 10241, filter_param);
        gl.glTexParameterx(texTarget, 10240, filter_param);
        return tex[0];
    }

    public static void deleteTex(int hTex) {
        int[] tex = new int[]{hTex};
        GLES10.glDeleteTextures(1, tex, 0);
    }

    public static void deleteTex(GL10 gl, int hTex) {
        int[] tex = new int[]{hTex};
        gl.glDeleteTextures(1, tex, 0);
    }

    public static int loadTextureFromResource(Context context, int resId) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 255, 0);
        Drawable background = context.getResources().getDrawable(resId);
        background.setBounds(0, 0, 256, 256);
        background.draw(canvas);
        int[] textures = new int[1];
        GLES10.glGenTextures(1, textures, 0);
        GLES10.glBindTexture(3553, textures[0]);
        GLES10.glTexParameterx(3553, 10241, 9728);
        GLES10.glTexParameterx(3553, 10240, 9729);
        GLES10.glTexParameterx(3553, 10242, 10497);
        GLES10.glTexParameterx(3553, 10243, 10497);
        GLUtils.texImage2D(3553, 0, bitmap, 0);
        bitmap.recycle();
        return textures[0];
    }

    public static int createTextureWithTextContent(String text) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 255, 0);
        Paint textPaint = new Paint();
        textPaint.setTextSize(32.0F);
        textPaint.setAntiAlias(true);
        textPaint.setARGB(255, 255, 255, 255);
        canvas.drawText(text, 16.0F, 112.0F, textPaint);
        int texture = initTex(3553, '蓀', 9728, 9729, 10497);
        GLUtils.texImage2D(3553, 0, bitmap, 0);
        bitmap.recycle();
        return texture;
    }

    public static void checkLocation(int location, String label) {
        if(location < 0) {
            throw new RuntimeException("Unable to locate \'" + label + "\' in program");
        }
    }

    @SuppressLint({"InlinedApi"})
    public static void logVersionInfo() {
        Log.i("GLHelper", "vendor  : " + GLES10.glGetString(7936));
        Log.i("GLHelper", "renderer: " + GLES10.glGetString(7937));
        Log.i("GLHelper", "version : " + GLES10.glGetString(7938));
        if(BuildCheck.isAndroid4_3()) {
            int[] values = new int[1];
            GLES30.glGetIntegerv('舛', values, 0);
            int majorVersion = values[0];
            GLES30.glGetIntegerv('舜', values, 0);
            int minorVersion = values[0];
            if(GLES30.glGetError() == 0) {
                Log.i("GLHelper", "version: " + majorVersion + "." + minorVersion);
            }
        }

    }

    public static String gluErrorString(int error) {
        switch(error) {
            case 0:
                return "no error";
            case 1280:
                return "invalid enum";
            case 1281:
                return "invalid value";
            case 1282:
                return "invalid operation";
            case 1283:
                return "stack overflow";
            case 1284:
                return "stack underflow";
            case 1285:
                return "out of memory";
            default:
                return null;
        }
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            Matrix.setLookAtM(scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
            GLES10.glMultMatrixf(scratch, 0);
        }
    }

    public static void gluOrtho2D(float left, float right, float bottom, float top) {
        GLES10.glOrthof(left, right, bottom, top, -1.0F, 1.0F);
    }

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        float top = zNear * (float)Math.tan((double)fovy * 0.008726646259971648D);
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
        GLES10.glFrustumf(left, right, bottom, top, zNear, zFar);
    }

    public static int gluProject(float objX, float objY, float objZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            boolean M_OFFSET = false;
            boolean V_OFFSET = true;
            boolean V2_OFFSET = true;
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            scratch[16] = objX;
            scratch[17] = objY;
            scratch[18] = objZ;
            scratch[19] = 1.0F;
            Matrix.multiplyMV(scratch, 20, scratch, 0, scratch, 16);
            float w = scratch[23];
            if(w == 0.0F) {
                return 0;
            } else {
                float rw = 1.0F / w;
                win[winOffset] = (float)view[viewOffset] + (float)view[viewOffset + 2] * (scratch[20] * rw + 1.0F) * 0.5F;
                win[winOffset + 1] = (float)view[viewOffset + 1] + (float)view[viewOffset + 3] * (scratch[21] * rw + 1.0F) * 0.5F;
                win[winOffset + 2] = (scratch[22] * rw + 1.0F) * 0.5F;
                return 1;
            }
        }
    }

    public static int gluUnProject(float winX, float winY, float winZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized(scratch) {
            boolean PM_OFFSET = false;
            boolean INVPM_OFFSET = true;
            boolean V_OFFSET = false;
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            if(!Matrix.invertM(scratch, 16, scratch, 0)) {
                return 0;
            } else {
                scratch[0] = 2.0F * (winX - (float)view[viewOffset + 0]) / (float)view[viewOffset + 2] - 1.0F;
                scratch[1] = 2.0F * (winY - (float)view[viewOffset + 1]) / (float)view[viewOffset + 3] - 1.0F;
                scratch[2] = 2.0F * winZ - 1.0F;
                scratch[3] = 1.0F;
                Matrix.multiplyMV(obj, objOffset, scratch, 16, scratch, 0);
                return 1;
            }
        }
    }
}
