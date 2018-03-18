package com.unwheeze.unwheezeapp.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

/**
 * Created by User on 16/03/2018.
 */

public interface BluetoothActions {

    void setConnectionState(int connectionState);
    void receivedCharacteristics(final String action,final BluetoothGattCharacteristic characteristic);
    void receivedUpdate(final String action);
}
