package com.example.fitnesstracker.activities.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.activities.MapActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class BluetoothActivity extends AppCompatActivity implements OnItemSelectedListener, Observer {
    boolean searchBt = true;
    BluetoothAdapter bluetoothAdapter;
    List<BluetoothDevice> pairedDevices = new ArrayList<>();
    boolean menuBool = false;
    boolean h7 = false;
    boolean normal = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_devices);
        DataHandler.getInstance().addObserver(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            h7 = true;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (DataHandler.getInstance().newValue) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.bluetooth)
                            .setMessage(R.string.bluetoothOff)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    bluetoothAdapter.enable();
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    listBT();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            searchBt = false;
                        }
                    }).show();
                } else {
                    listBT();
                }
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MapActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        DataHandler.getInstance().deleteObserver(this);
    }

    public void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void listBT() {
        if (searchBt) {
            final List<String> list = new ArrayList<>();
            list.add("");
            pairedDevices.addAll(bluetoothAdapter.getBondedDevices());
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    list.add(device.getName() + "\n" + device.getAddress());
                }
            }
            if (!h7) {
                final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                        if (!list.contains(device.getName() + "\n" + device.getAddress())) {
                            list.add(device.getName() + "\n" + device.getAddress());
                            pairedDevices.add(device);
                        }
                    }
                };

                Thread scannerBTLE = new Thread() {
                    public void run() {
                        bluetoothAdapter.startLeScan(leScanCallback);
                        try {
                            Thread.sleep(5000);
                            bluetoothAdapter.stopLeScan(leScanCallback);
                        } catch (InterruptedException e) {
                            Log.e("Main Activity", "ERROR: on scanning");
                        }
                    }
                };
                scannerBTLE.start();
            }

            Spinner spinner = findViewById(R.id.spinner);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setOnItemSelectedListener(this);
            spinner.setAdapter(dataAdapter);

            if (DataHandler.getInstance().getID() != 0 && DataHandler.getInstance().getID() < spinner.getCount())
                spinner.setSelection(DataHandler.getInstance().getID());
        }
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 != 0) {
            DataHandler.getInstance().setID(arg2);
            if (!h7 && ((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1]).getName().contains("H7") && DataHandler.getInstance().getReader() == null) {
                DataHandler.getInstance().setH7(new DeviceConnectionThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], this));
                h7 = true;
            } else if (!normal && DataHandler.getInstance().getH7() == null) {
                DataHandler.getInstance().setReader(new ConnectionThread((BluetoothDevice) pairedDevices.toArray()[arg2 - 1], this));
                DataHandler.getInstance().getReader().start();
                normal = true;
            }
            menuBool = true;
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void connectionError() {
        if (menuBool) {
            menuBool = false;
            final BluetoothActivity ac = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), getString(R.string.couldnotconnect), Toast.LENGTH_SHORT).show();
                    Spinner spinner = findViewById(R.id.spinner);
                    if (DataHandler.getInstance().getID() < spinner.getCount())
                        spinner.setSelection(DataHandler.getInstance().getID());

                    if (!h7) {
                        DataHandler.getInstance().setReader(null);
                        DataHandler.getInstance().setH7(new DeviceConnectionThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        h7 = true;
                    } else if (!normal) {
                        DataHandler.getInstance().setH7(null);
                        DataHandler.getInstance().setReader(new ConnectionThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        DataHandler.getInstance().getReader().start();
                        normal = true;
                    }
                }
            });
        }
    }

    public void update(Observable observable, Object data) {
        receiveData();
    }

    public void receiveData() {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView rpm = findViewById(R.id.rpm);
                rpm.setText(DataHandler.getInstance().getLastValue());

                TextView min = findViewById(R.id.min);
                min.setText(DataHandler.getInstance().getMin());

                TextView avg = findViewById(R.id.avg);
                avg.setText(DataHandler.getInstance().getAvg());

                TextView max = findViewById(R.id.max);
                max.setText(DataHandler.getInstance().getMax());
            }
        });
    }

    public void onStop() {
        super.onStop();
    }
}