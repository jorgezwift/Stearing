package com.android.jdc.stearing.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.BleServerManager;

public class ServerManager extends BleServerManager {


    public static final String TAG = "ServerManager";
    public static final UUID SVC_UUID = UUID.fromString("347b0001-7635-408b-8918-8ff3949ce592");
    //public static final UUID CAR1 = UUID.fromString("347b0012-7635-408b-8918-8ff3949ce592");
    //public static final UUID CAR2 = UUID.fromString("347b0013-7635-408b-8918-8ff3949ce592");
    //public static final UUID CAR3 = UUID.fromString("347b0014-7635-408b-8918-8ff3949ce592");
    //public static final UUID CAR4 = UUID.fromString("347b0019-7635-408b-8918-8ff3949ce592");
    public static final UUID CARTX = UUID.fromString("347b0032-7635-408b-8918-8ff3949ce592");
    public static final UUID CARRX = UUID.fromString("347b0031-7635-408b-8918-8ff3949ce592");
    public static final UUID CARSTR = UUID.fromString("347b0030-7635-408b-8918-8ff3949ce592");

    private BluetoothGattCharacteristic strC = null;

    public ServerManager(@NonNull final Context context) {
        super(context);
    }


    public BluetoothGattCharacteristic getSterzoCharacteristic() {
        return strC;
    }

    @NonNull
    @Override
    protected List<BluetoothGattService> initializeServer() {
        Log.i("ServerManager", "initServer START");
        byte[] init_data = new byte[]{(byte) 0xff};
        byte[] init_data2 = new byte[]{(byte) 0xff};
        byte[] init_data3 = new byte[]{(byte) 0xff};
        byte[] init_data4 = new byte[]{(byte) 0xff};

        strC = characteristic(CARSTR,
                //BluetoothGattCharacteristic.PROPERTY_INDICATE // properties
                BluetoothGattCharacteristic.PROPERTY_NOTIFY
                        | BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
                BluetoothGattCharacteristic.PERMISSION_WRITE, // permissions
                init_data4, // initial data
                cccd(), reliableWrite(), description("notifications for steering angle", false) // descriptors
        );

        return Collections.singletonList(
                service(SVC_UUID,

                        characteristic(CARTX,
                                BluetoothGattCharacteristic.PROPERTY_INDICATE // properties
                                        | BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
                                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ, // permissions
                                init_data4, // initial data
                                cccd(), reliableWrite(), description("TX", false) // descriptors
                        ),


                        characteristic(CARRX,
                                BluetoothGattCharacteristic.PROPERTY_WRITE // properties
                                        | BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
                                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ, // permissions
                                init_data4, // initial data
                                cccd(), reliableWrite(), description("RX", false) // descriptors
                        ),
                        strC
                )
        );
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        Log.println(priority, TAG, message);
    }
}
