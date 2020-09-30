package com.android.jdc.stearing.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.jdc.stearing.ble.BleAdvertiser;
import com.android.jdc.stearing.ble.BleAdvertiserCallback;
import com.android.jdc.stearing.ble.ServerManager;
import com.android.jdc.stearing.ble.SterzoBleManager;
import com.android.jdc.stearing.service.events.SterzoEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import no.nordicsemi.android.ble.observer.BondingObserver;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import no.nordicsemi.android.ble.observer.ServerObserver;

public class SterzoService extends Service implements ServerObserver, ConnectionObserver, BondingObserver {
    public static final ThreadPoolExecutor mPool = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private static final String TAG = "SterzoService";
    private static final int MAX_STEER_ANGLE = 35;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    public static final Executor DUAL_THREAD_EXECUTOR =
            Executors.newFixedThreadPool(2, sThreadFactory);
    private static volatile EventBus sEventBus;
    private final IBinder mBinder = new SterzoServiceBinder();
    protected volatile boolean mAbort = false;
    private ServerManager serverManager;
    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            final String stateString = "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + (state);
            Log.d("SterzoService", stateString);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    onBluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    onBluetoothDisabled();
                    break;
            }
        }
    };
    private SterzoBleManager manager;
    private float angle;
    private BleAdvertiserCallback bleAdvertiseCallback;

    public static EventBus getEventBus() {
        if (sEventBus == null) {
            synchronized (SterzoService.class) {
                if (sEventBus == null) {
                    sEventBus = new EventBus();
                }
            }
        }
        return sEventBus;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serverManager = new ServerManager(this);
        serverManager.setServerObserver(this);

        registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        bleAdvertiseCallback = new BleAdvertiserCallback();

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            onBluetoothEnabled();
        } else {

            bluetoothAdapter.enable();
        }
    }

    @Override
    public void onDestroy() {
        // Close the GATT server. If it hasn't been opened this method does nothing
        serverManager.close();
        serverManager = null;

    }

    protected void onBluetoothEnabled() {
        // First, open the server. onServerReady() will be called when all services were added.
        serverManager.open();
    }

    protected void onBluetoothDisabled() {
        serverManager.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onServerReady() {
        Log.i(TAG, "SERVER READY");
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManager.getAdapter().getBluetoothLeAdvertiser().startAdvertising(
                BleAdvertiser.settings(),
                BleAdvertiser.advertiseData(),
                bleAdvertiseCallback
        );

    }

    @Override
    public void onDeviceConnectedToServer(@NonNull BluetoothDevice device) {
        Log.i(TAG, "Device Connected");
        manager = new SterzoBleManager(this);
        manager.setConnectionObserver(this);
        manager.setBondingObserver(this);
        manager.useServer(serverManager);
        manager.connect(device)
                .enqueue();


        Timer timerManage = new Timer();
        timerManage.schedule(new SendKeepAlive(), 0, 50);
    }

    @Override
    public void onDeviceDisconnectedFromServer(@NonNull BluetoothDevice device) {
        Log.i(TAG, "Device Disconnected");
    }

    protected synchronized void postToEventBus(SterzoEvent wsEvent) {
        if (sEventBus == null) {
            if (sEventBus == null) {
                sEventBus = new EventBus();
            }
        }
        if (mAbort) {
            Log.e("EventBus", "tried to post on bus after aborting");
        }

        Log.i("postToEventBus", "" + wsEvent.getClass());
        sEventBus.removeStickyEvent(wsEvent.getClass());
        sEventBus.postSticky(wsEvent);
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onBondingRequired");
    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onBonded");
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onBondingFailed");
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onDeviceConnecting");
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onDeviceConnected");
    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        Log.i(TAG, "onDeviceFailedToConnect");
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onDeviceReady");
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.i(TAG, "onDeviceDisconnecting");
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        Log.i(TAG, "onDeviceDisconnected");
    }

    class SendKeepAlive extends TimerTask {
        public SendKeepAlive() {
        }

        public void run() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (Math.abs(angle) < 5) {
                            angle = 0;
                        }
                        manager.write(serverManager.getSterzoCharacteristic(), ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(angle).array(), 0, 4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            DUAL_THREAD_EXECUTOR.execute(thread);
            //thread.start();
        }
    }

    public class SterzoServiceBinder extends Binder {
        public SterzoService getService() {
            return SterzoService.this;
        }
    }

}
