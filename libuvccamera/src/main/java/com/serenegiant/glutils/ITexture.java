package com.serenegiant.glutils;

import java.io.IOException;

/**
 * Created by john on 2017/6/10.
 */

public interface ITexture {
    void release();

    void bind();

    void unbind();

    int getTexTarget();

    int getTexture();

    float[] getTexMatrix();

    void getTexMatrix(float[] var1, int var2);

    int getTexWidth();

    int getTexHeight();

    void loadTexture(String var1) throws NullPointerException, IOException;
}
