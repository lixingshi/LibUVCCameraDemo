package com.equityinfo.john.libuvccamerademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.proxy.USBDeviceNameCallback;
import com.serenegiant.proxy.USBMonitorProxy;
import com.serenegiant.proxy.VideoPathCallback;
import com.serenegiant.widget.UVCCameraTextureView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button start_btn,stop_btn,takeVideo_btn,takeVideo_bg_btn;
    private TextView devName_tv;
    private USBMonitorProxy usbMonitorProxy;
    private UVCCameraTextureView cameraTextureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_btn = (Button) findViewById(R.id.start_btn);
        stop_btn = (Button) findViewById(R.id.stop_btn);
        takeVideo_btn = (Button) findViewById(R.id.takeVideo_btn);
        takeVideo_bg_btn = (Button) findViewById(R.id.takeVideo_bg_btn);
        devName_tv = (TextView) findViewById(R.id.devName_tv);
        start_btn.setOnClickListener(this);
        stop_btn.setOnClickListener(this);
        takeVideo_btn.setOnClickListener(this);
        takeVideo_bg_btn.setOnClickListener(this);
        cameraTextureView = (UVCCameraTextureView) findViewById(R.id.camera_view);

        usbMonitorProxy = new USBMonitorProxy(this);
        usbMonitorProxy.USBDeviceName(new USBDeviceNameCallback() {
            @Override
            public void name(String name) {
                devName_tv.setText(name);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (! Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,1);
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        usbMonitorProxy.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usbMonitorProxy.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbMonitorProxy.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    return;
                }
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ,Manifest.permission.RECORD_AUDIO,Manifest.permission.SYSTEM_ALERT_WINDOW},2);
                }

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_btn:
                usbMonitorProxy.startPreview(cameraTextureView);
                break;
            case R.id.takeVideo_btn:
                usbMonitorProxy.startTakeVideo();
                break;
            case R.id.takeVideo_bg_btn:
                usbMonitorProxy.startBackgoundTakeVideo();
                break;
            case R.id.stop_btn:
                usbMonitorProxy.stopTakeVideo(new VideoPathCallback() {
                    @Override
                    public void path(String path) {
                        Toast.makeText(MainActivity.this,path,Toast.LENGTH_SHORT).show();
                    }
                });

                usbMonitorProxy.stopBackgoundTakeVideo(new VideoPathCallback() {
                    @Override
                    public void path(String path) {
                        Toast.makeText(MainActivity.this,path,Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }
}
