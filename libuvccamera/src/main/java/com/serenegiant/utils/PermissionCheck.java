package com.serenegiant.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by john on 2017/6/10.
 */

public final class PermissionCheck {
    public PermissionCheck() {
    }

    public static final void dumpPermissions(Context context) {
        if(context != null) {
            try {
                PackageManager e = context.getPackageManager();
                List list = e.getAllPermissionGroups(128);
                Iterator var3 = list.iterator();

                while(var3.hasNext()) {
                    PermissionGroupInfo info = (PermissionGroupInfo)var3.next();
                    Log.d("PermissionCheck", info.name);
                }
            } catch (Exception var5) {
                Log.w("", var5);
            }

        }
    }

    @SuppressLint({"NewApi"})
    public static boolean hasPermission(Context context, String permissionName) {
        if(context == null) {
            return false;
        } else {
            boolean result = false;

            try {
                int e;
                if(BuildCheck.isMarshmallow()) {
                    e = context.checkSelfPermission(permissionName);
                } else {
                    PackageManager pm = context.getPackageManager();
                    e = pm.checkPermission(permissionName, context.getPackageName());
                }

                switch(e) {
                    case -1:
                    default:
                        break;
                    case 0:
                        result = true;
                }
            } catch (Exception var5) {
                Log.w("", var5);
            }

            return result;
        }
    }

    public static boolean hasAudio(Context context) {
        return hasPermission(context, "android.permission.RECORD_AUDIO");
    }

    public static boolean hasNetwork(Context context) {
        return hasPermission(context, "android.permission.INTERNET");
    }

    public static boolean hasWriteExternalStorage(Context context) {
        return hasPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE");
    }

    @SuppressLint({"InlinedApi"})
    public static boolean hasReadExternalStorage(Context context) {
        return BuildCheck.isAndroid4()?hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE"):hasPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE");
    }

    public static boolean hasAccessLocation(Context context) {
        return hasPermission(context, "android.permission.ACCESS_COARSE_LOCATION") && hasPermission(context, "android.permission.ACCESS_FINE_LOCATION");
    }

    public static boolean hasAccessCoarseLocation(Context context) {
        return hasPermission(context, "android.permission.ACCESS_COARSE_LOCATION");
    }

    public static boolean hasAccessFineLocation(Context context) {
        return hasPermission(context, "android.permission.ACCESS_FINE_LOCATION");
    }

    public static boolean hasCamera(Context context) {
        return hasPermission(context, "android.permission.CAMERA");
    }

    public static void openSettings(Context context) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        Uri uri = Uri.fromParts("package", context.getPackageName(), (String)null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static List<String> missingPermissions(Context context, String[] expectations) throws IllegalArgumentException, PackageManager.NameNotFoundException {
        return missingPermissions(context, (List)(new ArrayList(Arrays.asList(expectations))));
    }

    public static List<String> missingPermissions(Context context, List<String> expectations) throws IllegalArgumentException, PackageManager.NameNotFoundException {
        if(context != null && expectations != null) {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 4096);
            String[] info = pi.requestedPermissions;
            if(info != null) {
                String[] var5 = info;
                int var6 = info.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String i = var5[var7];
                    expectations.remove(i);
                }
            }

            return expectations;
        } else {
            throw new IllegalArgumentException("context or expectations is null");
        }
    }
}
