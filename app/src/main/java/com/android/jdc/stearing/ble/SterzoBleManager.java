package com.android.jdc.stearing.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import no.nordicsemi.android.ble.BleManager;

public class SterzoBleManager extends BleManager {

    public static final String TAG = "SterzoBleManager";

    static byte[] challengeRequest = new byte[]{(byte) 0x03, (byte) 0x10, (byte) 0x12, (byte) 0x34};
    static byte[] someOtherThing = new byte[]{(byte) 0x03, (byte) 0x11, (byte) 0xFF, (byte) 0xFF};
    static byte[] constantSt = new byte[]{(byte) 0x00, (byte) 0x8c, (byte) 0x87, (byte) 0xbe};

    boolean sendSteeringData = false;

    public SterzoBleManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    public void write(@Nullable final BluetoothGattCharacteristic characteristic,
                      @Nullable final byte[] data, int a, int b) {
        if (sendSteeringData) {

            byte[] data2 = new byte[4];
            data2[0] = (byte) 0x00;
            data2[1] = data[0];
            data2[2] = data[1];
            data2[3] = (byte) 0x3f;
            sendNotification(characteristic, data, a, b).enqueue();
            Log.i(TAG, "data: " + Arrays.toString(data));
            Log.i(TAG, "data2: " + Arrays.toString(data2));
        }
    }

    private class MyManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void onServerReady(@NonNull final BluetoothGattServer server) {
            Log.i(TAG, "onServerReady");

            BluetoothGattCharacteristic serverCharacteristicRx = server
                    .getService(ServerManager.SVC_UUID)
                    .getCharacteristic(ServerManager.CARRX);
            BluetoothGattCharacteristic serverCharacteristicTx = server
                    .getService(ServerManager.SVC_UUID)
                    .getCharacteristic(ServerManager.CARTX);

            setWriteCallback(serverCharacteristicRx)
                    .with((device, data) ->
                            {
                                // if we got 0x0310
                                if (data.getByte(0) == 0x03 && data.getByte(1) == 0x10) {
                                    // issue the challenge of 0x0310yyyy on 0x0032
                                    Log.i(TAG, "got request for challenge");
                                    sendIndication(serverCharacteristicTx, challengeRequest, 0, 4).enqueue();
                                }
                                if (data.getByte(0) == 0x03 && data.getByte(1) == 0x11) {
                                    Log.i(TAG, "got thing2");
                                    sendIndication(serverCharacteristicTx, someOtherThing, 0, 4).enqueue();
                                    SterzoBleManager.this.sendSteeringData = true;
                                }
                            }

                    );
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            // [...]
            //serverCharacteristic = null;
        }
    }
}
