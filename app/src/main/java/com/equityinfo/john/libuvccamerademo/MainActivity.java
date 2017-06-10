package com.equityinfo.john.libuvccamerademo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;

public class MainActivity extends AppCompatActivity {
    USBMonitor monitor;
    TextView textView;
    WindowManager windowManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.mtextView);
        monitor = new USBMonitor(this,onDeviceConnectListener);
        monitor.register();

        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        TextureView v = new TextureView(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(v,lp);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        monitor.unregister();
    }

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {

        }

        @Override
        public void onDettach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {

        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };
}
