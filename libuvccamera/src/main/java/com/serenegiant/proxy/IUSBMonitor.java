package com.serenegiant.proxy;

import android.hardware.usb.UsbDevice;

import java.util.List;

/**
 * Created by john on 2017/6/10.
 */

public interface IUSBMonitor {
    void destroy();
    void unregister();
    void register();
    int getDeviceCount();
    List<UsbDevice> getDeviceList();
    boolean hasPermission(final UsbDevice device);
    boolean isRegistered();
    boolean requestPermission(final UsbDevice device);
}
