package com.serenegiant.utils;

/**
 * Created by john on 2017/6/9.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Handler.Callback;

public class HandlerThreadHandler extends Handler {
    private static final String TAG = "HandlerThreadHandler";

    public static final HandlerThreadHandler createHandler() {
        return createHandler("HandlerThreadHandler");
    }

    public static final HandlerThreadHandler createHandler(String name) {
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        return new HandlerThreadHandler(thread.getLooper());
    }

    public static final HandlerThreadHandler createHandler(Callback callback) {
        return createHandler("HandlerThreadHandler", callback);
    }

    public static final HandlerThreadHandler createHandler(String name, Callback callback) {
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        return new HandlerThreadHandler(thread.getLooper(), callback);
    }

    private HandlerThreadHandler(Looper looper) {
        super(looper);
    }

    private HandlerThreadHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }
}

