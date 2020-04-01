package com.stubit.cocktailremote;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.stubit.cocktailremote.adapters.BluetoothDeviceAdapter;
import com.stubit.cocktailremote.bluetooth.BluetoothManager;


public class BluetoothDevicePickerActivity extends AppCompatActivity {
    public static final String BLUETOOTH_DEVICE = "bluetooth_device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView deviceList = findViewById(R.id.bluetooth_device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(new BluetoothDeviceAdapter(this, bluetoothDevice -> {
            Intent bluetoothDeviceAddress = new Intent();
            bluetoothDeviceAddress.putExtra(BLUETOOTH_DEVICE, bluetoothDevice.getAddress());

            setResult(RESULT_OK, bluetoothDeviceAddress);
            finish();
        }));

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothManager.getInstance().cleanup(this);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
