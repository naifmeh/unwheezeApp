package com.unwheeze.unwheezeapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by User on 16/03/2018.
 */

public class BluetoothUtils {

    private Context mCtx;
    private BluetoothAdapter mBtAdapter;


    public BluetoothUtils(Context context, BluetoothAdapter btAdapter) {
        this.mCtx = context;
        this.mBtAdapter = btAdapter;
    }

}
