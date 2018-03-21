package com.unwheeze.unwheezeapp.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

/**
 * Created by User on 16/03/2018.
 */

public class MyBluetoothGattCallback extends BluetoothGattCallback {
    private static final String TAG = MyBluetoothGattCallback.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private BluetoothActions mBtActions;

    public final static String ACTION_BTE_CONNECTED = "ACTION_BTE_CONNECTED";
    public final static String ACTION_BTE_DISCONNECTED = "ACTION_BTE_DISCONNECTED";
    public final static String ACTION_BTE_SERVICES_DISCOVERED = "ACTION_BTE_SERVICES_DISCOVERED";
    public final static String ACTION_BTE_AVAILABLE = "ACTION_BTE_AVAILABLE";
    public final static String ACTION_BTE_CHARACS_DISCOVERED = "ACTION_BTE_CHARACS_DISCOVERED";

    public MyBluetoothGattCallback(BluetoothActions actions) {
        mBtActions = actions;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
       String action;
       if(newState == BluetoothProfile.STATE_CONNECTED) {
           action = ACTION_BTE_CONNECTED;
           mBtActions.setConnectionState(STATE_CONNECTED);
           mBtActions.receivedUpdate(action);
           Log.i(TAG, "Connected to GATT server.");
           Log.i(TAG, "Attempting to start service discovery:" +
                   gatt.discoverServices());

       } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
           action = ACTION_BTE_DISCONNECTED;
           Log.i(TAG, "Disconnected from GATT server.");
           mBtActions.setConnectionState(STATE_DISCONNECTED);
           mBtActions.receivedUpdate(action);

       }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG,"Writing characteristic : "+new String(characteristic.getValue()));
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if(status == BluetoothGatt.GATT_SUCCESS) {
            mBtActions.receivedUpdate(ACTION_BTE_SERVICES_DISCOVERED);
            for(BluetoothGattService service : gatt.getServices()) {
                Log.d(TAG,"Service UUID :"+service.getUuid().toString());
                for(BluetoothGattCharacteristic charac : service.getCharacteristics()) {
                    String charact = charac.getUuid().toString();
                    Log.d(TAG,"Charac UUID :"+charact);
                    for(BluetoothGattDescriptor descriptor : charac.getDescriptors()) {
                        Log.d(TAG,"Descriptor UUID :"+descriptor.getUuid().toString());
                    }
                }
            }

            mBtActions.receivedUpdate(ACTION_BTE_CHARACS_DISCOVERED);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG,"READ DATA FROM DESCRIPTOR : "+descriptor.getUuid().toString());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG,"WROTE DATA FROM DESCRIPTOR : "+descriptor.getUuid().toString());
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mBtActions.receivedCharacteristics(ACTION_BTE_AVAILABLE,characteristic);
        Log.i(TAG,"Charac notifs triggered");
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(status == BluetoothGatt.GATT_SUCCESS) {
            mBtActions.receivedCharacteristics(ACTION_BTE_AVAILABLE,characteristic);
        }
    }
}
