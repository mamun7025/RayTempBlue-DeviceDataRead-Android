package com.example.raytemp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

import uk.co.etiltd.thermalib.Device;
import uk.co.etiltd.thermalib.ThermaLib;
import uk.co.etiltd.thermalib.ThermaLibException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static int REQUEST_PERMISSIONS = 1001;

    ThermaLib therm;
    List<Device> deviceList;

    public static boolean scanCompleteFlag = false;
    final Handler mHandler = new Handler();
    final int delayInMillis = 5000;     // 1000 milliseconds == 1 second

    ListView listView;
    List<String> listItem;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.MyListView);

        // save sample test data
        // sendDataByHttpGet_sample();

        listItem = new ArrayList<>();
        listItem.add("Sample Device-1");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String clickDeviceIdentifier = adapter.getItem(position);
                Toast.makeText(getApplicationContext(),clickDeviceIdentifier,Toast.LENGTH_SHORT).show();
                // connect to click device
                if(deviceList != null){
                    for (Device device: deviceList){
                        String deviceName = device.getDeviceName();
                        String identifier = device.getIdentifier();
                        String deviceIdentifier = deviceName + "-" + identifier;
                        if(deviceIdentifier.equals(clickDeviceIdentifier)){
                            try {
                                device.requestConnection();
                            } catch (ThermaLibException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivity.this, "Connection request sent to device: " + deviceIdentifier, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        // Handle run time permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getAllPermission()) initializeThermaLib();
        } else {
            initializeThermaLib();
        }

        // new code
        Button mButtonScan = findViewById(R.id.buttonScan);
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                therm.startScanForDevices(ThermaLib.Transport.BLUETOOTH_LE, 5); // TransportType, TimeoutInSeconds
                // reset
                scanCompleteFlag = false;
                // call after scan complete
                startTimerToChekScanComplete();
            }
        });
    }

    private void startTimerToChekScanComplete(){

        Runnable runnableInst = new Runnable() {
            @Override
            public void run() {
                if(scanCompleteFlag){
                    System.out.println("@Scan Complete**********************************************");
                    deviceList = therm.getDeviceList();
                    System.out.println(deviceList);
                    for (Device device: deviceList){
                        String deviceName = device.getDeviceName();
                        String identifier = device.getIdentifier();
                        String deviceIdentifier = deviceName + "-" + identifier;
                        listItem.add(deviceIdentifier);
                        Toast.makeText(MainActivity.this, "Found device: " + device.getDeviceName() + " Serial: " + device.getSerialNumber(), Toast.LENGTH_LONG).show();
                    }
                    adapter.notifyDataSetChanged();
//                    mHandler.postDelayed(this, delayInMillis);
//                    mHandler.removeCallbacks(runnableInst);
                } else {
                    mHandler.postDelayed(this, delayInMillis); // Optional, to repeat the task.
                }
            }
        };
        // start
        mHandler.postDelayed(runnableInst, delayInMillis);
        // mHandler.removeCallbacks(runnableInst);
    }


    private void initializeThermaLib() {
        // ThermaLib therm = ThermaLib.instance(this);
        therm = ThermaLib.instance(this);
        RxRayTempCallbacks callbacksHC = new RxRayTempCallbacks(this, therm);
        therm.registerCallbacks(callbacksHC, TAG);
        // Ref: uk.co.etiltd.thermalib.ac -> Line 142
        therm.startScanForDevices(ThermaLib.Transport.BLUETOOTH_LE, 5); // TransportType, TimeoutInSeconds
    }

    private boolean getAllPermission() {

        List<String> list = new ArrayList<>();

        int fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (coarseLocation != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            int bluetoothScan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            int bluetoothConnect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

            if (bluetoothScan != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            if (bluetoothConnect != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!list.isEmpty()) {
            final int size = list.size();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(list.toArray(new String[size]), REQUEST_PERMISSIONS);
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            // Check if all permissions are granted
            boolean allGranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allGranted = true;
                } else {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                initializeThermaLib();
            } else {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Bluetooth & Location permission");
                builder.setPositiveButton("Grant", (dialog, which) -> {
                    dialog.cancel();
                    getAllPermission();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                    finish();
                });
                builder.show();
            }
        }
    }


    /*public void sendDataByHttpGet_sample() {
        Thread callHttpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calling HTTP ---------------------------------------------Start");
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://localhost:8080/app?dd=123"); // dd = device data
                    connection = (HttpURLConnection) url.openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    String body = sb.toString();
                    System.out.println(body);
                    Log.d("HTTP-GET", body);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assert connection != null;
                    connection.disconnect();
                }
                System.out.println("Calling HTTP--------------------------------------------End");
            }
        });
        callHttpThread.start();
    }*/

}