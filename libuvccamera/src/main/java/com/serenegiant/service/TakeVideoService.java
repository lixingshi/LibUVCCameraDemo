package com.serenegiant.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by john on 2017/6/9.
 */

public class TakeVideoService extends Service {
    private final String TAG = "TakeVideoService";
    public final static int MSG_STOPVIDEO = 0;
    public final static int MSG_READY = 1;
    public final static int MSG_FILEPATH = 2;
    public final static int MSG_TAKEVIDEO = 3;
    public final static int MSG_CONN = 4;
    private WindowManager windowManager;
    private UVCCameraTextureView mUVCCameraView;
    private USBMonitor mUSBMonitor;
    private UVCCameraHandler mCameraHandler;
    private Messenger clientMessenger;
    private volatile UsbDevice mDevice;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mUVCCameraView = new UVCCameraTextureView(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(mUVCCameraView,lp);
        mUSBMonitor = new USBMonitor(this,mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this,mUVCCameraView,1,640,480,1);
        mCameraHandler.addCallback(cameraCallback);
        mUSBMonitor.register();
    }

    private Messenger mMessenger = new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msgfromClient) {
            clientMessenger = msgfromClient.replyTo;
            switch (msgfromClient.what){
                case MSG_STOPVIDEO:
                    mCameraHandler.stopRecording();
                    break;
                case MSG_TAKEVIDEO:
                    mCameraHandler.startRecording();
                    break;
                case MSG_CONN:
                    if (mDevice != null){
                        mUSBMonitor.requestPermission(mDevice);
                    }
                    break;
            }
            super.handleMessage(msgfromClient);
        }
    });

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
    }

    private USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            mDevice = device;
            mUSBMonitor.requestPermission(device);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            mDevice = null;
        }

        @Override
        public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (clientMessenger != null){
                mCameraHandler.open(ctrlBlock);

                final SurfaceTexture st =  mUVCCameraView.getSurfaceTexture();
                mCameraHandler.startPreview(new Surface(st));
                Message msg = Message.obtain();
                msg.what = TakeVideoService.MSG_READY;
                try {
                    clientMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (mCameraHandler != null){
                mCameraHandler.close();
            }
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private AbstractUVCCameraHandler.CameraCallback cameraCallback = new AbstractUVCCameraHandler.CameraCallback() {
        @Override
        public void onOpen() {
           // Toast.makeText(getApplicationContext(),"onOpen",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClose() {

        }

        @Override
        public void onStartPreview() {
            //Toast.makeText(getApplicationContext(),"onStartPreview",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopPreview() {

        }

        @Override
        public void onStartRecording() {
            //Toast.makeText(getApplicationContext(),"onStartRecording",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopRecording() {

        }

        @Override
        public void OutputFilePath(String path) {
            if (clientMessenger != null){
                Message msg = Message.obtain();
                msg.what = TakeVideoService.MSG_FILEPATH;
                msg.obj = path;
                try {
                    clientMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onError(Exception e) {
            //Toast.makeText(getApplicationContext(),"onError",Toast.LENGTH_SHORT).show();
        }
    };

}
