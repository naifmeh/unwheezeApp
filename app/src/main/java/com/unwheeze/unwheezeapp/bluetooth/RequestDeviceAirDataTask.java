package com.unwheeze.unwheezeapp.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.activities.MainActivity;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.network.AirDataRequestSingleton;
import com.unwheeze.unwheezeapp.network.RequestsScheme;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 16/03/2018.
 */

public class RequestDeviceAirDataTask extends AsyncTask<AirData,Void,Void> {
    private static final String TAG = RequestDeviceAirDataTask.class.getSimpleName();

    private WeakReference<Activity> weakActivity;
    private SharedPreferences mSharedPreferences;
    private SQLiteDatabase mDb;

    private AirDataDbHelper mDbHelper;
    private RequestQueue queue;
    private boolean canStartWork = false;

    private Uri mUri;

    public RequestDeviceAirDataTask(MainActivity myActivity) {
        super();
        weakActivity = new WeakReference<>(myActivity);
        if(weakActivity.get() != null) {
            mDbHelper = new AirDataDbHelper(weakActivity.get());
            Context context = weakActivity.get();
            mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_prefs_file_key),context.MODE_PRIVATE);
            canStartWork = true;

            mDb = new AirDataDbHelper(context).getWritableDatabase();
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(RequestsScheme.HTTP_SCHEME)
                .encodedAuthority(RequestsScheme.AUTHORITY)
                .encodedPath(RequestsScheme.APP_PATH)
                .appendPath(RequestsScheme.AIRDATA_PATH)
                .appendPath(RequestsScheme.AIRDATA_PUTAIRDATA);

        mUri = builder.build();
        Log.d(TAG,mUri.toString());
        queue = AirDataRequestSingleton.getInstance(myActivity.getApplicationContext())
                .getRequestQueue();
    }


    @Override
    protected Void doInBackground(AirData... airData) {
        if(!canStartWork) return null;
        if(airData.length > 1) return null;
        if(mDb == null) return null;
        Log.d(TAG,(new Gson()).toJson(airData[0]));

        /* Insert into Database */

        mDb.execSQL(AirDataUtils.insertDataSql(airData[0]));

        StringRequest postRequest = new StringRequest(Request.Method.POST, mUri.toString(), (response)->Log.d(TAG,response), (error) -> {}) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                String api_key="none";
                String sharedApiKey = weakActivity.get().getString(R.string.shared_prefs_file_api_key);
                if(mSharedPreferences != null && mSharedPreferences.contains(sharedApiKey)) {
                    api_key = mSharedPreferences.getString(sharedApiKey,"none");
                }
                map.put("X-Api-Key",api_key);

                return map;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return (new Gson()).toJson(airData[0]).getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        queue.add(postRequest);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mDb.close();
    }
}
