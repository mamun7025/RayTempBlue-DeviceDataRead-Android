package com.example.appsup4vx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import uk.co.etiltd.thermalib.ThermaLib;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static int REQUEST_LOCATION = 1001;

    private static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permission ACCESS_COARSE_LOCATION
        checkPermission();
    }

    private void initializeThermaLib() {
        ThermaLib therm = ThermaLib.instance(this);
        RxRayTempCallbacks callbacksHC = new RxRayTempCallbacks(this);
        therm.registerCallbacks(callbacksHC, TAG);
        therm.startScanForDevices(ThermaLib.Transport.BLUETOOTH_LE);

        // handling connection and data reading
        callbacksHC.setThermInst(therm);
    }

    // Function to check and request permission.
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        } else {
            initializeThermaLib();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeThermaLib();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}