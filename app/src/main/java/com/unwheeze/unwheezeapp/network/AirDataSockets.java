package com.unwheeze.unwheezeapp.network;

import android.support.annotation.Nullable;
import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by User on 20/03/2018.
 */

public class AirDataSockets extends WebSocketListener {
    private static final String TAG = AirDataSockets.class.getSimpleName();

    private AirDataSocketsListener mListener;

    public AirDataSockets(AirDataSocketsListener listener) {
        mListener = listener;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.d(TAG,"Connection opened");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        mListener.onDataReceived(null);
        super.onMessage(webSocket, text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG,"Received a new message");
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
    }
}
