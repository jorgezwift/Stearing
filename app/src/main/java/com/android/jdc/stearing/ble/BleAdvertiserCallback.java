package com.android.jdc.stearing.ble;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

public class BleAdvertiserCallback extends AdvertiseCallback {

    private final static String TAG = "BleAdCallback";

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        Log.i(TAG, "LE Advertise Started.");
    }

    @Override
    public void onStartFailure(int errorCode) {
        Log.w(TAG, "LE Advertise Failed: $errorCode");
    }
}
