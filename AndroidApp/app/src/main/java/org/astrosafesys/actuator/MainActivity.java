package org.astrosafesys.actuator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    Switch switch_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switch_button = findViewById(R.id.Cancello);
        switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    cancello();
                //else
                //    fUncheckSwitch();
            }
        });
    }

    void cancello() {

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .setTimeout(5000)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( "CDB7950D-73F1-4D4D-8E47-C090502DBD63" ) );
        //ParcelUuid pUuid = new ParcelUuid( UUID.randomUUID());

        byte[] rData = new byte[4];
        rData[0] = 29;  //Action
        rData[1] = 14;  //ID
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
                fUncheckSwitch();
            }
        }, 5000);

    }

    void fUncheckSwitch(){
        Log.i("BLE", "Stop Advertising");
        switch_button.setChecked(false);

    }
}
