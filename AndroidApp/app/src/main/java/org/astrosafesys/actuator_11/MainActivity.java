package org.astrosafesys.actuator_11;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static int TIMEOUT_TIME = 3000;

    BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    Switch switch_button_gate;
    Switch switch_button_garage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switch_button_gate = findViewById(R.id.Cancello);
        switch_button_gate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    sendAdvertiseData(14);
                //else
                //    fResetSwitch();
            }
        });

        switch_button_garage = findViewById(R.id.Garage);
        switch_button_garage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    sendAdvertiseData(40);
                //else
                //    fResetSwitch();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.getApplicationContext().checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, 666);  // Comment 26
                return;
            }
            if (this.getApplicationContext().checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 666);  // Comment 26
                return;
            }
        }

    }

    private boolean fCheckBluetoothDevice(){
        boolean rRet=false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {

                mBluetoothAdapter.enable();
                // Bluetooth is not enable :)
            }else
                rRet=true;

        }
        return rRet;
    }

    void sendAdvertiseData(int pDeviceID) {
        if(!fCheckBluetoothDevice()) {
            fResetSwitch();
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .setTimeout(TIMEOUT_TIME)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( "CDB7950D-73F1-4D4D-8E47-C090502DBD63" ) );
        //ParcelUuid pUuid = new ParcelUuid( UUID.randomUUID());

        byte[] rData = new byte[4];
        rData[0] = 29;  //Action
        rData[1] = (byte)pDeviceID;  //ID
        rData[2] = (byte) 0xAB; //Postfix
        rData[3] = (byte) 0xAA; //Postfix

        //.addServiceUuid( pUuid )
        //                .addServiceData( pUuid,  rData)
        //"Data".getBytes( Charset.forName( "UTF-8" ) )
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addManufacturerData(1024, rData)
                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.e( "BLE", "Advertising onStartSuccess " );
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fResetSwitch();
            }
        }, TIMEOUT_TIME);

    }

    /**
     * Reset button state
     */
    void fResetSwitch(){
        Log.i("BLE", "Stop Advertising");
        switch_button_gate.setChecked(false);

    }
}
