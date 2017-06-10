package com.serenegiant.glutils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by john on 2017/6/10.
 */

public interface IRendererCommon {
    int MIRROR_NORMAL = 0;
    int MIRROR_HORIZONTAL = 1;
    int MIRROR_VERTICAL = 2;
    int MIRROR_BOTH = 3;
    int MIRROR_NUM = 4;

    void setMirror(int var1);

    int getMirror();

    @Retention(RetentionPolicy.SOURCE)
    public @interface MirrorMode {
    }
}
