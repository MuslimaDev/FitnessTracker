package com.example.fitnesstracker.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

@SuppressLint("NewApi")
public class DeviceConnectionThread extends Thread {
    private BluetoothActivity activity;
    private BluetoothGatt gatt;
    private static BluetoothGattDescriptor descriptor;
    private static BluetoothGattCharacteristic characteristic;

    DeviceConnectionThread(BluetoothDevice device, BluetoothActivity bluetoothActivity) {
        this.activity = bluetoothActivity;
        BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
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
                String HRUUID = "0000180D-0000-1000-8000-00805F9B34FB";
                BluetoothGattService service = gatt.getService(UUID.fromString(HRUUID));
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic cc : characteristics) {
                    for (BluetoothGattDescriptor descriptor : cc.getDescriptors()) {
                        DeviceConnectionThread.descriptor = descriptor;
                        DeviceConnectionThread.characteristic = cc;
                        gatt.setCharacteristicNotification(cc, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        };
        gatt = device.connectGatt(bluetoothActivity, false, bluetoothGattCallback);
    }

    public void cancel() {
        gatt.setCharacteristicNotification(characteristic, false);
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        gatt.disconnect();
        gatt.close();
    }
}