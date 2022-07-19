package com.example.appsup4vx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import uk.co.etiltd.thermalib.ThermaLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permission ACCESS_COARSE_LOCATION
        checkCoarsePermission();

        ThermaLib therm = ThermaLib.instance(this);
        RxRayTempCallbacks callbacksHC = new RxRayTempCallbacks(this);
        therm.registerCallbacks(callbacksHC, "MainActivity");
        therm.startScanForDevices(ThermaLib.Transport.BLUETOOTH_LE);

        // handling connection and data reading
        callbacksHC.setThermInst(therm);
    }

    private void checkCoarsePermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
        }
    }

}