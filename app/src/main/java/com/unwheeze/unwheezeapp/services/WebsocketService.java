package com.unwheeze.unwheezeapp.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.network.AirDataSockets;
import com.unwheeze.unwheezeapp.network.AirDataSocketsListener;
import com.unwheeze.unwheezeapp.network.RequestsScheme;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by User on 20/03/2018.
 */

public class WebsocketService extends Service implements AirDataSocketsListener{
    private static final String TAG = WebSocketListener.class.getSimpleName();

    private Thread mWsThread;
    private Runnable mRunnable;
    private AirDataSockets mAirDataSockets;
    private WebSocket mWs;
    private OkHttpClient mClient;

    public class WebSocketBinder extends Binder {
        public WebsocketService getService() {
            return WebsocketService.this;
        }
        
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"Started service");
        mRunnable = ()-> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                mClient.dispatcher().executorService().shutdown();
        };
        mAirDataSockets = new AirDataSockets(this);
        mClient =  new OkHttpClient();
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(RequestsScheme.WS_SCHEME)
                .encodedAuthority(RequestsScheme.AUTHORITY)
                .appendPath(RequestsScheme.WS_REALTIME)
                .appendPath(RequestsScheme.WS_AIRDATA);
        Uri uri = uriBuilder.build();

        Request request = new Request.Builder().url(uri.toString()).build();

        mWs = mClient.newWebSocket(request,mAirDataSockets);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mWsThread = new Thread(mRunnable);
        mWsThread.start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return new WebSocketBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWsThread = null;
    }

    @Override
    public void onDataReceived(AirData airData)
    {
        Intent newDataIntent = new Intent();
        newDataIntent.setAction(getString(R.string.websocketDataReceivedIntent));
        Log.d(TAG,"Ws listener called from service");
    }
}
