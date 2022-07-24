package com.example.raytemp;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.etiltd.thermalib.Device;
import uk.co.etiltd.thermalib.Sensor;
import uk.co.etiltd.thermalib.ThermaLib;

import java.io.IOException;


public class RxRayTempCallbacks extends ThermaLib.ClientCallbacksBase{

    private final Context mContext;
    private final ThermaLib therm;


    public RxRayTempCallbacks(Context context, ThermaLib therm) {
        this.mContext = context;
        this.therm = therm;
        System.out.println("@context calling********************************************************");
    }

    @Override
    public void onNewDevice(Device device, long timestamp) {
        System.out.println("@onNewDevice***********************************************************S");
        System.out.println(device);
        System.out.println(timestamp);
        System.out.println("@onNewDevice***********************************************************E");
    }

    public void onDeviceDeleted(String deviceAddress, int transportType) {
    }

    public void onDeviceConnectionStateChanged(Device device, Device.ConnectionState newState, long timestamp) {
        System.out.println("@onDeviceConnectionStateChanged***************************************S");
        System.out.println(device);
        System.out.println(timestamp);
        System.out.println("@onDeviceConnectionStateChanged***************************************E");
    }

    public void onBatteryLevelReceived(Device device, int levelPercent, long timestamp) {
        System.out.println("@onBatteryLevelReceived************************************************S");
        System.out.println(levelPercent);
        System.out.println("@onBatteryLevelReceived************************************************E");
    }

    public void onDeviceUpdated(Device device, long timestamp) {
//        System.out.println("@onDeviceUpdated...........");
    }

    public void onRefreshComplete(Device device, boolean userRefresh, long timestamp) {
        System.out.println("@.... xx found....");
    }

    public void onScanComplete(int errorCode, int numDevices) {
        System.out.println("@onScanComplete********************************************************S");
        System.out.println(errorCode);
        System.out.println(numDevices);
        System.out.println("@onScanComplete********************************************************E");
    }

    public void onScanComplete(int transport, ThermaLib.ScanResult scanResult, int numDevices, String errorMsg) {
        System.out.println("@onScanComplete2********************************************************S");
        System.out.println(transport);
        System.out.println(scanResult);
        System.out.println(numDevices);
        System.out.println(errorMsg);
        MainActivity.scanCompleteFlag = true;
        // save device list data
//        saveDeviceListData(therm.getDeviceList());
        sendDeviceListData(therm.getDeviceList());
        System.out.println("@onScanComplete2********************************************************E");
    }

    public void onMessage(Device device, String msg, long timestamp) {
        System.out.println("@onMessage********************************************************" + msg);
    }

    public void onDeviceReady(Device device, long timestamp) {
        System.out.println("@onDeviceReady*********************************************************");
        Toast.makeText(mContext, "Device: " + device.getDeviceName() + ", ready to send data ", Toast.LENGTH_LONG).show();
    }

    public void onDeviceNotificationReceived(Device device, int notificationType, byte[] payload, long timestamp) {
        System.out.println("@onDeviceNotificationReceived******************************************S");
        System.out.println(device);
        System.out.println(notificationType);
        if (notificationType == 1) {
            Sensor sensor = device.getSensors().get(0);
            String unitDesc = sensor.getDisplayUnit().getDesc();
            String unitString = sensor.getDisplayUnit().getUnitString();
            // show
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.getDefault());
            String toastString = "Device Data: "+ sensor.getReading() + " " + unitString + " " + unitDesc + ", " + sdf.format(date);
            System.out.println(toastString);
            Toast.makeText(mContext, toastString, Toast.LENGTH_LONG).show();
            String readingData = sensor.getReading() + " " + unitString + " " + unitDesc;
            // send device reading data
            sendDeviceReadingData(readingData, device.getDeviceName());
        }
        System.out.println("@onDeviceNotificationReceived******************************************E");
    }

    public void onRssiUpdated(Device device, int rssi) {
        System.out.println("@onRssiUpdated**********************************************************");
    }

    public void onUnexpectedDeviceDisconnection(Device device, long timestamp) {
        System.out.println("****************************************@onUnexpectedDeviceDisconnection");
    }

    public void onUnexpectedDeviceDisconnection(Device device, String exceptionMessage, ThermaLib.ClientCallbacks.DeviceDisconnectionReason reason, long timestamp) {
        System.out.println("***************************************@onUnexpectedDeviceDisconnection");
    }

    public void onRequestServiceComplete(int transport, boolean succeeded, String errorMessage, String appKey) {
        System.out.println("***********************************************@onRequestServiceComplete");
    }

    public void onDeviceAccessRequestComplete(Device device, boolean succeeded, String errorMessage) {
        System.out.println("******************************************@onDeviceAccessRequestComplete");
    }

    public void onDeviceRevokeRequestComplete(Device device, boolean succeeded, String errorMessage) {
        System.out.println("******************************************@onDeviceRevokeRequestComplete");
    }

    public void onRemoteSettingsReceived(Device device) {
        System.out.println("***********************************************@onRemoteSettingsReceived");
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom methods
    public void sendDeviceListData(List<Device> deviceList){
        Thread callHttpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calling HTTP ---------------------------------------------Start");
                StringBuilder sbDeviceList = new StringBuilder();
                for (Device device: deviceList){
                    String deviceName = device.getDeviceName();
                    sbDeviceList.append(deviceName);
                    sbDeviceList.append(";");
                }

                byte[] postData = sbDeviceList.toString().getBytes( StandardCharsets.UTF_8 );

                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL("http://localhost:8080/app?dl=1"); // dl = device list
                    httpConn = (HttpURLConnection) url.openConnection();
                    // For POST only - START
                    httpConn.setDoOutput( true );
                    httpConn.setRequestMethod("POST");
                    // httpConn.getOutputStream().write(postData);
                    OutputStream os = httpConn.getOutputStream();
                    os.write(postData);
                    os.flush();
                    os.close();
                    // For POST only - END
                    // read response
                    int responseCode = httpConn.getResponseCode();
                    System.out.println("nSending 'POST' request to URL : " + url);
                    System.out.println("Post Data : " + Arrays.toString(postData));
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response .append(line);
                    }
                    br.close();
                    String responseBody = response.toString();
                    // printing result from response
                    System.out.println(responseBody);
                    Log.d("HTTP-POST", responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assert httpConn != null;
                    httpConn.disconnect();
                }
                System.out.println("Calling HTTP--------------------------------------------End");
            }
        });
        callHttpThread.start();
    }

    public void sendDeviceReadingData(String readingData, String deviceName){
        Thread callHttpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calling HTTP ---------------------------------------------Start");
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://localhost:8080/app?dd="+readingData+"&dn="+deviceName); // dd = device data, dn = device name
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
    }


}