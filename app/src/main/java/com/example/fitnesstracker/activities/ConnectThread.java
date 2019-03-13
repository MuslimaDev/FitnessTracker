package com.example.fitnesstracker.activities;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectThread extends Thread {
    BluetoothAdapter bluetoothAdapter;
    private final BluetoothSocket socket;
    BluetoothActivity activity;

    public ConnectThread(BluetoothDevice device, BluetoothActivity ac) {
        this.activity = ac;
        BluetoothSocket tmp = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            UUID MY_UUID = UUID.fromString("d8336105-1bd9-4b0b-88b0-f7644dafb19d");
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException ignored) {
            Log.e("ConnectThread", "Error on getting the device");
        }
        socket = tmp;
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();
        int ok = 0;
        while (ok < 2)
            try {
                if (socket.isConnected())
                    socket.close();
                socket.connect();
                ok = 5;
            } catch (IOException connectException) {
                if (ok == 0)
                    ok++;
                else {
                    activity.connectionError();

                    try {
                        socket.close();
                    } catch (IOException closeException) {
                        Log.e("ConnectThread", "Error on getting the stack");
                    }
                    return;
                }
            }

        while (true) {
            try {
                DataHandler.getInstance().acqui(socket.getInputStream().read());
            } catch (IOException e) {
                activity.connectionError();

                try {
                    socket.getInputStream().close();
                    socket.close();
                } catch (IOException closeException) {
                    Log.e("ConnectThread", "Error on getting the stack");
                }
                return;
            }
        }
    }

    public void cancel() {
        try {
            if (socket != null && socket.isConnected())
                socket.close();
        } catch (IOException e) {
            Log.e("ConnectThread", "Error on closing bluetooth");
        }
    }
}