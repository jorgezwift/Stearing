package com.android.jdc.stearing;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.jdc.stearing.service.SterzoService;
import com.android.jdc.stearing.service.events.SterzoEvent;
import com.android.jdc.stearing.voice.CustomRecognizer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, CustomRecognizer.VoiceRecognizerListener {

    public static final String TAG = "MainActivity";
    public static final int REQUEST_MICROPHONE = 10001;
    boolean mBound = false;
    private AlertDialog accessDenied;
    private SterzoService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {


            Log.i("mConnection", "Service Connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SterzoService.SterzoServiceBinder binder = (SterzoService.SterzoServiceBinder) service;
            mService = binder.getService();
            mService.getEventBus().removeAllStickyEvents();
            if (!mService.getEventBus().isRegistered(MainActivity.this)) {
                Log.i("EVENT_BUS", "Register");
                mService.getEventBus().registerSticky(MainActivity.this);
            }

            if (mService != null) {
                try {
                    //mService.registerActivity(Installation.id(StartActivity.this), listener);
                } catch (Throwable t) {
                }
            }
            //if ( !mService.isConnected()) {
            //TODO mService.startConnection();
            //}else {
            //TODO Execute Something111111
            //}

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            //mService.unregisterActivity(Installation.id(StartActivity.this));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
*/


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_MICROPHONE);

        } else
            startAll();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void startAll() {


        Intent intent = new Intent(this, SterzoService.class);
        startService(intent);
        Log.i("EVENT_BUS", "Bind");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);/**/
        mBound = true;

        startRecording();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_MICROPHONE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_MICROPHONE:
                if (grantResults != null && grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAll();
                } else {


                    if (accessDenied == null) {
                        accessDenied = new AlertDialog.Builder(this)
                                .setTitle("Denied")
                                .setMessage("You need to accept the permissions!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_MICROPHONE);
                                        accessDenied.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MainActivity.this.finish();
                                    }
                                }).create();
                    }

                    accessDenied.show();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onEventMainThread(SterzoEvent wsEvent) {
        Log.i(TAG, "" + wsEvent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        float angle = (float) (i - 35);
        Log.i(TAG, "Seek angle: " + angle);
        mService.setAngle(angle);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    void startRecording() {
        String rec = PackageManager.FEATURE_MICROPHONE;

        if (rec != "android.hardware.microphone") {
            // no microphone, no recording. Disable the button and output an alert
            Toast.makeText(this, "NO MICROPHONE", Toast.LENGTH_SHORT);
        } else {

            //you can pass any object you want to connect to your recognizer here (I am passing the activity)
            CustomRecognizer voice = new CustomRecognizer(this);
            voice.StartListening();

        }
    }

    @Override
    public void onAllCommand() {

        Log.i(TAG, "onAllCommand");
        mService.setAngle(20);
    }

    @Override
    public void onNoneCommand() {

        Log.i(TAG, "onNoneCommand");
        mService.setAngle(-20);
    }

    @Override
    public void onLeftCommand() {

        Log.i(TAG, "onLeftCommand");
        mService.setAngle(-10);
    }

    @Override
    public void onRightCommand() {

        Log.i(TAG, "onRightCommand");
        mService.setAngle(10);
    }

    @Override
    public void onGoCommand() {
        Log.i(TAG, "onGoCommand");
        mService.setAngle(0);
    }
}