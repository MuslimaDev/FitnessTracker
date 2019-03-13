package com.example.fitnesstracker.activities;

import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

@SuppressLint("NewApi")
public class H7ConnectThread extends Thread {
    BluetoothActivity activity;
    private BluetoothGatt gat;
    private final String HRUUID = "d8336105-1bd9-4b0b-88b0-f7644dafb19d";
    static BluetoothGattDescriptor descriptor;
    static BluetoothGattCharacteristic characteristic;

    public H7ConnectThread(BluetoothDevice device, BluetoothActivity ac) {
        this.activity = ac;
        gat = device.connectGatt(ac, false, btleGattCallback);
    }

    public void cancel() {
        gat.setCharacteristicNotification(characteristic, false);
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        gat.writeDescriptor(descriptor);
        gat.disconnect();
        gat.close();
        Log.i("H7ConnectThread", "Closing HRsensor");
    }

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            int bmp = data[1] & 0xFF;
            DataHandler.getInstance().cleanInput(bmp);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                activity.connectionError();
            } else {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            BluetoothGattService service = gatt.getService(UUID.fromString(HRUUID));
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic cc : characteristics) {
                for (BluetoothGattDescriptor descriptor : cc.getDescriptors()) {
                    H7ConnectThread.descriptor = descriptor;
                    H7ConnectThread.characteristic = cc;
                    gatt.setCharacteristicNotification(cc, true);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }
    };
}