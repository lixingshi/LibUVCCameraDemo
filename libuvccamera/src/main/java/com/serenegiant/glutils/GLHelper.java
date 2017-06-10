package com.serenegiant.glutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.serenegiant.utils.AssetsHelper;
import com.serenegiant.utils.BuildCheck;

import java.io.IOException;

/**
 * Created by john on 2017/6/10.
 */

public final class GLHelper {
    private static final String TAG = "GLHelper";

    public GLHelper() {
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
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
        GLES20.glActiveTexture(texUnit);
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(texTarget, tex[0]);
        GLES20.glTexParameteri(texTarget, 10242, wrap);
        GLES20.glTexParameteri(texTarget, 10243, wrap);
        GLES20.glTexParameteri(texTarget, 10241, min_filter);
        GLES20.glTexParameteri(texTarget, 10240, mag_filter);
        return tex[0];
    }

    public static void deleteTex(int hTex) {
        int[] tex = new int[]{hTex};
        GLES20.glDeleteTextures(1, tex, 0);
    }

    public static int loadTextureFromResource(Context context, int resId) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 255, 0);
        Drawable background = context.getResources().getDrawable(resId);
        background.setBounds(0, 0, 256, 256);
        background.draw(canvas);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(3553, textures[0]);
        GLES20.glTexParameterf(3553, 10241, 9728.0F);
        GLES20.glTexParameterf(3553, 10240, 9729.0F);
        GLES20.glTexParameterf(3553, 10242, 10497.0F);
        GLES20.glTexParameterf(3553, 10243, 10497.0F);
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

    public static int loadShader(Context context, String vss_asset, String fss_asset) {
        int program = 0;

        try {
            String vss = AssetsHelper.loadString(context.getAssets(), vss_asset);
            String fss = AssetsHelper.loadString(context.getAssets(), vss_asset);
            program = loadShader(vss, fss);
        } catch (IOException var6) {
            ;
        }

        return program;
    }

    public static int loadShader(String vss, String fss) {
        int[] compiled = new int[1];
        int vs = loadShader('謱', vss);
        if(vs == 0) {
            return 0;
        } else {
            int fs = loadShader('謰', fss);
            if(fs == 0) {
                return 0;
            } else {
                int program = GLES20.glCreateProgram();
                checkGlError("glCreateProgram");
                if(program == 0) {
                    Log.e("GLHelper", "Could not create program");
                }

                GLES20.glAttachShader(program, vs);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(program, fs);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, '讂', linkStatus, 0);
                if(linkStatus[0] != 1) {
                    Log.e("GLHelper", "Could not link program: ");
                    Log.e("GLHelper", GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    return 0;
                } else {
                    return program;
                }
            }
        }
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, '讁', compiled, 0);
        if(compiled[0] == 0) {
            Log.e("GLHelper", "Could not compile shader " + shaderType + ":");
            Log.e("GLHelper", " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    public static void checkLocation(int location, String label) {
        if(location < 0) {
            throw new RuntimeException("Unable to locate \'" + label + "\' in program");
        }
    }

    @SuppressLint({"InlinedApi"})
    public static void logVersionInfo() {
        Log.i("GLHelper", "vendor  : " + GLES20.glGetString(7936));
        Log.i("GLHelper", "renderer: " + GLES20.glGetString(7937));
        Log.i("GLHelper", "version : " + GLES20.glGetString(7938));
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
}

