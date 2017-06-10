package com.serenegiant.glutils;

/**
 * Created by john on 2017/6/10.
 */

public interface IDrawer2D {
    void release();

    float[] getMvpMatrix();

    IDrawer2D setMvpMatrix(float[] var1, int var2);

    void getMvpMatrix(float[] var1, int var2);

    void draw(int var1, float[] var2, int var3);

    void draw(ITexture var1);

    void draw(TextureOffscreen var1);
}
