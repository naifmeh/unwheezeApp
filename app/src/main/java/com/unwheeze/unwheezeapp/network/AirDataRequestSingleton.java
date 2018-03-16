package com.unwheeze.unwheezeapp.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by User on 15/03/2018.
 */

public class AirDataRequestSingleton {

    private static AirDataRequestSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private AirDataRequestSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized AirDataRequestSingleton getInstance(Context context) {
        if(mInstance == null)
            mInstance = new AirDataRequestSingleton(context);

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            //Pour eviter de tourner sur le contexte de l'activité
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
}
