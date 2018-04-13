package com.example.dell.imeidemo.Util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.example.dell.imeidemo.MainActivity;

/**
 * Created by dell on 2018/4/8.
 */

public class SomeUtils {

    //在设备号前面补0，补到20位
    public static String coverDid(String did) {
        int coverNum = 20 - did.length();
        String coverStr = "";
        for (int i = 0; i < coverNum; i++) {
            coverStr += "0";
        }
        return coverStr + did;
    }
}
