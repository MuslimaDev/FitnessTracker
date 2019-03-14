package com.example.fitnesstracker.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectionThread extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothSocket socket;
    private BluetoothActivity activity;

    ConnectionThread(BluetoothDevice device, BluetoothActivity bluetoothActivity) {
        this.activity = bluetoothActivity;
        BluetoothSocket bluetoothSocket = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException ignored) {
            Log.e("ConnectionThread", "Error on getting the device");
        }
        socket = bluetoothSocket;
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
                        Log.e("ConnectionThread", "Error on getting the stack");
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
                    Log.e("ConnectionThread", "Error on getting the stack");
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
            Log.e("ConnectionThread", "Error on closing bluetooth");
        }
    }
}