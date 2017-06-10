package com.serenegiant.glutils;

/**
 * Created by john on 2017/6/10.
 */

import com.serenegiant.glutils.IDrawer2D;

public interface IDrawer2dES2 extends IDrawer2D {
    int glGetAttribLocation(String var1);

    int glGetUniformLocation(String var1);

    void glUseProgram();
}
