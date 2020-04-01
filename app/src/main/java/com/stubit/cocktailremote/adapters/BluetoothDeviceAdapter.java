package com.stubit.cocktailremote.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.stubit.cocktailremote.R;
import com.stubit.cocktailremote.bluetooth.BluetoothManager;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    final MutableLiveData<Set<BluetoothDevice>> mBoundDevices = new MutableLiveData<>();
    final ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    final OnSelectListener mOnSelectListener;

    public BluetoothDeviceAdapter(AppCompatActivity app, OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;

        BluetoothManager.getInstance().getBondDevices(app, mBoundDevices);
        mBoundDevices.observe(app, boundDevices -> {

            mDevices.clear();
            mDevices.addAll(boundDevices);

            notifyDataSetChanged();
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mNameView.setText(mDevices.get(position).getName());
        holder.mAddressView.setText(mDevices.get(position).getAddress());

        holder.mHolder.setOnClickListener(v -> {
            mOnSelectListener.run(mDevices.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        final View mHolder;
        final TextView mNameView, mAddressView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mHolder = itemView;
            mNameView = mHolder.findViewById(R.id.device_name);
            mAddressView = mHolder.findViewById(R.id.device_address);
        }
    }

    public interface OnSelectListener {
        void run(BluetoothDevice bluetoothDevice);
    }
}
