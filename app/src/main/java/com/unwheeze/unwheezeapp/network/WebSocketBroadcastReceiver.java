package com.unwheeze.unwheezeapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by User on 21/03/2018.
 */

public class WebSocketBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = WebSocketBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"WebSocketBroadcastReceiver.onReceive");
    }
}
