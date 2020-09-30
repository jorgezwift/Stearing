package com.android.jdc.stearing.ble;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;

public class BleAdvertiser {

    private final static String TAG = "BleAdvertiser";

    public static AdvertiseSettings settings() {

        return new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();
    }

    public static AdvertiseData advertiseData() {
        return new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(ServerManager.SVC_UUID))
                .build();
    }
}


