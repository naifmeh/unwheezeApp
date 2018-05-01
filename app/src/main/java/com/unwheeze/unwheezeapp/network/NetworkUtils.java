package com.unwheeze.unwheezeapp.network;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Network;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 28/04/2018.
 */

public class NetworkUtils {
    private final static String TAG = NetworkUtils.class.getSimpleName();

    private Context mCtx;
    private static RequestQueue mQueue;
    private Uri.Builder mBuilder;
    private SharedPreferences mSharedPrefs;

    public interface NetworkActionListener {
        void onResponseResult(JsonArray jsonArray);
    }

    public NetworkUtils(@NonNull Context context) {
        mCtx = context;

        mSharedPrefs = mCtx.getSharedPreferences(mCtx.getString(R.string.shared_prefs_file_key),mCtx.MODE_PRIVATE);
        mQueue = AirDataRequestSingleton.getInstance(mCtx).getRequestQueue();

        mBuilder = new Uri.Builder();
        mBuilder.scheme(RequestsScheme.HTTP_SCHEME)
                .encodedAuthority(RequestsScheme.AUTHORITY)
                .appendPath(RequestsScheme.APP_PATH)
                .appendPath(RequestsScheme.AIRDATA_PATH);
    }

    public void getNearest(double latitude,double longitude,@Nullable String radius,NetworkActionListener listener) {
        mBuilder.appendPath(RequestsScheme.AIRDATA_GETNEAREST);
        String location = Double.toString(latitude)+","+Double.toString(longitude);
        mBuilder.appendEncodedPath(location);
        if(radius != null) mBuilder.appendPath(radius);
        Uri uri = mBuilder.build();

        JsonArray jsonArray = new JsonArray();
        Gson gson = new Gson();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,uri.toString(),null,(response)-> {
            jsonArray.addAll(gson.fromJson(response.toString(), JsonArray.class));
            listener.onResponseResult(jsonArray);
            Log.d(TAG,jsonArray.toString());
        },(error) -> {
            //TODO: Handle error
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("X-Api-Key",getApiKeyFromPrefs());

                return map;
            }
        };
        mQueue.add(jsonArrayRequest);
    }

    public void getAllAirDataElements(NetworkActionListener listener) {
        mBuilder.appendPath(RequestsScheme.AIRDATA_GETALL);
        Uri uri = mBuilder.build();

        JsonArray jsonArray = new JsonArray();
        Gson gson = new Gson();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,uri.toString(),null,(response)-> {
            jsonArray.addAll(gson.fromJson(response.toString(), JsonArray.class));
            listener.onResponseResult(jsonArray);
            Log.d(TAG,jsonArray.toString());
        },(error) -> {
            //TODO: Handle error
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("X-Api-Key",getApiKeyFromPrefs());

                return map;
            }
        };
        mQueue.add(jsonArrayRequest);
    }

    public String getApiKeyFromPrefs() {
        String api_key="none";
        String sharedApiKey = mCtx.getString(R.string.shared_prefs_file_api_key);
        if(mSharedPrefs != null && mSharedPrefs.contains(sharedApiKey)) {
            api_key = mSharedPrefs.getString(sharedApiKey,"none");
        }

        return api_key;
    }
}
