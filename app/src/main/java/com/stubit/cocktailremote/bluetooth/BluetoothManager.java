package com.stubit.cocktailremote.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.*;

public class BluetoothManager {
    public static final String TAG = "BluetoothManager";

    static private BluetoothManager mInstance;

    private final BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBluetoothEnabledReceiver;
    private BluetoothSocket mBluetoothSocket;

    private String mUsedBluetoothAddress;

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            throw new RuntimeException("No bluetooth adapter available");
        }
    }

    public static BluetoothManager getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothManager();
        }

        return mInstance;
    }

    public String getConnectedDeviceAddress() {
        return mUsedBluetoothAddress;
    }

    public void cleanup(final AppCompatActivity app) {
        removeBluetoothEvenReceiver(app);
    }

    public void getBondDevices(AppCompatActivity app, MutableLiveData<Set<BluetoothDevice>> devices) {
        enableBluetooth(app, () -> {
            if (devices != null) {
                devices.postValue(mBluetoothAdapter.getBondedDevices());
            }
        });
    }

    public void send(final AppCompatActivity app, @NotNull String deviceAddress, String string, Runnable onFailure) {
        enableBluetooth(app, () -> connect(deviceAddress, () -> writeToBluetoothOutputStream(string.getBytes(), onFailure), onFailure));
    }

    public void send(final AppCompatActivity app, @NotNull String AddressDevice, Integer integer, Runnable onFailure) {
        enableBluetooth(app, () -> connect(AddressDevice, () -> writeToBluetoothOutputStream(integer, onFailure), onFailure));
    }

    public void send(final AppCompatActivity app, @NotNull String deviceAddress, byte[] bytes, Runnable onFailure) {
        enableBluetooth(app, () -> connect(deviceAddress, () -> {
            if(bytes != null) {
                writeToBluetoothOutputStream(bytes, onFailure);
            }
        }, onFailure));
    }

    private void writeToBluetoothOutputStream(byte[] bytes, Runnable onFailure) {
        try {
            mBluetoothSocket.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();

            if(onFailure != null) {
                onFailure.run();
            }
        }
    }

    private void writeToBluetoothOutputStream(int bytes, Runnable onFailure) {
        try {
            mBluetoothSocket.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();

            if(onFailure != null) {
                onFailure.run();
            }
        }
    }

    private void enableBluetooth(@NotNull final AppCompatActivity app, Runnable whenEnabled) {
        if (!mBluetoothAdapter.isEnabled()) {
            removeBluetoothEvenReceiver(app);

            mBluetoothEnabledReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int currentBluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                    Log.d(
                            TAG,
                            "Bluetooth state changed from " +
                                    stateToString(intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)) +
                                    " to " +
                                    stateToString(currentBluetoothState)
                    );

                    if (currentBluetoothState == STATE_ON) {
                        removeBluetoothEvenReceiver(app);

                        whenEnabled.run();
                    }
                }
            };

            app.registerReceiver(mBluetoothEnabledReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            app.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            whenEnabled.run();
        }
    }

    private void removeBluetoothEvenReceiver(@NotNull AppCompatActivity app) {
        if (mBluetoothEnabledReceiver != null) {
            app.unregisterReceiver(mBluetoothEnabledReceiver);
            mBluetoothEnabledReceiver = null;

            Log.d(TAG, "Bluetooth broadcast receiver removed");
        }
    }

    @NotNull
    private String stateToString(final int state) {
        switch (state) {
            case STATE_OFF:
                return "OFF";
            case STATE_TURNING_ON:
                return "TURNING ON";
            case STATE_ON:
                return "ON";
            case STATE_TURNING_OFF:
                return "TURNING OFF";
            default:
                return String.valueOf(state);
        }
    }

    private void connect(String deviceAddress, @NotNull Runnable whenConnected, Runnable onFailure) {
        try {
            connect(deviceAddress);
            whenConnected.run();
        } catch (IOException e) {
            e.printStackTrace();

            if(onFailure != null) {
                onFailure.run();
            }
        }
    }

    private void disconnect() throws IOException {
        if (mBluetoothSocket != null) {
            mBluetoothSocket.getInputStream().close();
            mBluetoothSocket.getOutputStream().close();
            mBluetoothSocket.close();

            mBluetoothSocket = null;
            mUsedBluetoothAddress = null;

            Log.d(TAG, "Disconnected bluetooth device");
        }
    }

    private void connect(@NotNull String deviceAddress) throws IOException {
        if (mUsedBluetoothAddress == null || !mUsedBluetoothAddress.equals(deviceAddress)) {
            disconnect();

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            mBluetoothAdapter.cancelDiscovery();

            mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            mBluetoothSocket.connect();

            mUsedBluetoothAddress = deviceAddress;
            Log.d(TAG, "Connected bluetooth device");
        }
    }
}
