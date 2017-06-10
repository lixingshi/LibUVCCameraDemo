package com.serenegiant.proxy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import com.serenegiant.service.TakeVideoService;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.utils.PermissionCheck;
import com.serenegiant.widget.CameraViewInterface;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by john on 2017/6/10.
 */

public final class USBMonitorProxy implements IUSBMonitor{
    private static final String TAG = "USBMonitorProxy";
    private final int VIDEO_ENCODERTYPE = 1;
    private final int PREVIEW_WIDTH = 640;
    private final int PREVIEW_HEIGHT = 480;
    private final int MUXER_FORMATTYPE = 1;
    private IUSBMonitor mUsbMonitor;
    private CameraViewInterface mUVCCameraView;
    private UVCCameraHandler mCameraHandler;
    private WeakReference<Activity> mContext;
    private Context mApplicationContext;
    private volatile UsbDevice mUsbDevice;
    private Messenger mService;
    private USBDeviceNameCallback usbDeviceNameCallback;
    private VideoPathCallback videoPathCallback;
    private volatile boolean isServiceConn = false;

    public USBMonitorProxy(@NonNull Activity context){
        this.mContext = new WeakReference<Activity>(context);
        this.mApplicationContext = context.getApplicationContext();
        this.mUsbMonitor = new USBMonitor(context,onDeviceConnectListener);
        register();
    }

    public synchronized boolean startPreview(@NonNull CameraViewInterface mUVCCameraView){
        if (mCameraHandler != null && mCameraHandler.isPreviewing()){
            return false;
        }
        this.mUVCCameraView = mUVCCameraView;
        final Context context = mContext.get();
        if (context == null){
            return false;
        }
        this.mCameraHandler = UVCCameraHandler.createHandler(context,mUVCCameraView
                ,VIDEO_ENCODERTYPE,PREVIEW_WIDTH,PREVIEW_HEIGHT,MUXER_FORMATTYPE);
        mCameraHandler.addCallback(cameraCallback);
        if (!mCameraHandler.isPreviewing() && this.mUsbDevice != null){
            mUsbMonitor.requestPermission(this.mUsbDevice);
            Log.d(TAG,"startPreview");
            return true;
        }else{
            return false;
        }
    }

    public synchronized boolean startTakeVideo(){
        if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
            if (!mCameraHandler.isRecording()) {
                mCameraHandler.startRecording();
                Log.d(TAG,"startRecording");
                return true;
            }
        }
        return false;
    }

    public void stopTakeVideo(@NonNull VideoPathCallback callback){
        if (mCameraHandler == null){
            return;
        }
        if (mCameraHandler.isRecording()){
            mCameraHandler.stopRecording();
            this.videoPathCallback = callback;
        }
    }

    public synchronized void startBackgoundTakeVideo(){
        if (mService != null){
            return;
        }

        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }

        if (mApplicationContext == null){
            return;
        }

        mApplicationContext.getApplicationContext().bindService(new Intent(mApplicationContext, TakeVideoService.class),conn,Context.BIND_AUTO_CREATE);
    }

    public synchronized void stopBackgoundTakeVideo(VideoPathCallback callback){
        if (mApplicationContext == null || mService == null){
            return;
        }
        this.videoPathCallback = callback;

        Message msg = Message.obtain();
        msg.what = TakeVideoService.MSG_STOPVIDEO;
        msg.replyTo = mMessenger;
        if (isServiceConn){
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void USBDeviceName(@NonNull USBDeviceNameCallback callback){
        this.usbDeviceNameCallback = callback;
    }

    public List<String> getDeviceNameList(){
        return null;
    }

    private Messenger mMessenger = new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what){
                case TakeVideoService.MSG_FILEPATH:
                    String filePath = (String) msgFromServer.obj;
                    if (videoPathCallback != null){
                        videoPathCallback.path(filePath);
                    }
                    mApplicationContext.unbindService(conn);
                    mService = null;
                    break;
                case TakeVideoService.MSG_READY:
                    Message msg = Message.obtain();
                    msg.what = TakeVideoService.MSG_TAKEVIDEO;
                    msg.replyTo = mMessenger;
                    if (isServiceConn){
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message msg = Message.obtain();
            msg.what = TakeVideoService.MSG_CONN;
            msg.replyTo = mMessenger;

            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            isServiceConn = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isServiceConn = false;
        }
    };

    private boolean checkPermissionWriteExternalStorage() {
        final Context context = mContext.get();
        if (context == null){
            return false;
        }
        if (!PermissionCheck.hasWriteExternalStorage(context)) {
            return false;
        }
        return true;
    }

    private boolean checkPermissionAudio() {
        final Context context = mContext.get();
        if (context == null){
            return false;
        }
        if (!PermissionCheck.hasAudio(context)) {
           return false;
        }
        return true;
    }

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            mUsbDevice = device;
            final String name = String.format("UVC Camera:(%x:%x:%s)", device.getVendorId(), device.getProductId(), device.getDeviceName());
            final Activity context = mContext.get();
            if (context == null){
                return;
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (usbDeviceNameCallback != null){
                        usbDeviceNameCallback.name(name);
                    }
                }
            });

        }

        @Override
        public void onDettach(UsbDevice device) {
            mUsbDevice = null;
            final Activity context = mContext.get();
            if (context == null){
                return;
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (usbDeviceNameCallback != null){
                        usbDeviceNameCallback.name("设备断开");
                    }
                }
            });
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            mCameraHandler.open(ctrlBlock);
            final SurfaceTexture st =  mUVCCameraView.getSurfaceTexture();
            mCameraHandler.startPreview(new Surface(st));
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };

    private AbstractUVCCameraHandler.CameraCallback cameraCallback = new AbstractUVCCameraHandler.CameraCallback() {
        @Override
        public void onOpen() {

        }

        @Override
        public void onClose() {

        }

        @Override
        public void onStartPreview() {

        }

        @Override
        public void onStopPreview() {

        }

        @Override
        public void onStartRecording() {

        }

        @Override
        public void onStopRecording() {

        }

        @Override
        public void OutputFilePath(String path) {
            if (videoPathCallback != null){
                videoPathCallback.path(path);
            }
        }

        @Override
        public void onError(Exception e) {

        }
    };

    public void start(){
        register();
		if (mUVCCameraView != null){
			mUVCCameraView.onResume();
		}
    }

    public void stop(){
        if (mCameraHandler != null){
            mCameraHandler.close();
            if (mUVCCameraView != null){
                mUVCCameraView.onPause();
            }
        }
    }

    @Override
    public void destroy() {
        if (mCameraHandler != null) {
			mCameraHandler.release();
			mCameraHandler = null;
		}
		if (mUsbMonitor != null) {
            mUsbMonitor.destroy();
            mUsbMonitor = null;
		}
		mUVCCameraView = null;
    }

    @Override
    public void unregister() {
        if (this.mUsbMonitor != null){
            if (this.mUsbMonitor.isRegistered()){
                this.mUsbMonitor.unregister();
            }
        }
    }

    @Override
    public void register() {
        if (this.mUsbMonitor != null){
            if (!this.mUsbMonitor.isRegistered()){
                this.mUsbMonitor.register();
            }
        }
    }

    @Override
    public int getDeviceCount() {
        if (this.mUsbMonitor != null){
            return this.mUsbMonitor.getDeviceCount();
        }
        return 0;
    }

    @Override
    public List<UsbDevice> getDeviceList() {
        if (this.mUsbMonitor != null){
            return this.mUsbMonitor.getDeviceList();
        }
        return null;
    }

    @Override
    public boolean hasPermission(UsbDevice device) {
        if (this.mUsbMonitor != null && device != null){
            return this.mUsbMonitor.hasPermission(device);
        }
        return false;
    }

    @Override
    public boolean isRegistered() {
        if (this.mUsbMonitor != null){
            return this.mUsbMonitor.isRegistered();
        }
        return false;
    }

    @Override
    public boolean requestPermission(UsbDevice device) {
        if (this.mUsbMonitor != null && device != null){
            return this.mUsbMonitor.requestPermission(device);
        }
        return false;
    }
}
