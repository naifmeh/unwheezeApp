package com.unwheeze.unwheezeapp.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 14/03/2018.
 */

public class AirDataLoader extends AsyncTaskLoader<String> {

    private static final String TAG = AirDataLoader.class.getSimpleName();

    private AirDataDbHelper mDbHelper = new AirDataDbHelper(getContext());
    private SQLiteDatabase db = mDbHelper.getWritableDatabase();

    private RequestQueue queue;

    private Uri mUri;

    public AirDataLoader(Context context) {
        super(context);
        queue = AirDataRequestSingleton.getInstance(context.getApplicationContext())
            .getRequestQueue();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(RequestsScheme.HTTP_SCHEME)
                .encodedAuthority(RequestsScheme.AUTHORITY)
                .appendPath(RequestsScheme.APP_PATH)
                .appendPath(RequestsScheme.AIRDATA_PATH)
                .appendPath(RequestsScheme.AIRDATA_GETALL);

        mUri = builder.build();


    }

    @Override
    public void onCanceled(String data) {
        super.onCanceled(data);
    }

    @Override
    public String loadInBackground() {
        Log.d(TAG,"Loading in background "+mUri.toString());

        final Gson gson = new Gson();

        final ContentValues values = new ContentValues();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, mUri.toString(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JsonArray arrayReps = new JsonArray();
                arrayReps.addAll(gson.fromJson(response.toString(),JsonArray.class));

                for(int i=0;i<arrayReps.size();i++) {
                        String jsonObj = arrayReps.get(i).toString();
                        populateContentValue(values,gson.fromJson(jsonObj,AirData.class));
                        db.insert(AirDataContract.AirDataEntry.TABLE_NAME,null,values);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                //TODO(2) : Get API key from sharedPrefs
                params.put("X-Api-Key","b8f1e1cc-58af-43f8-acc0-f1adde406a4b");

                return params;
            }
        };

        queue.add(jsonArrayRequest);
        return null;
    }

    private void populateContentValue(ContentValues values,AirData airData) {
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_ID,airData.getUserID());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_PM10,airData.getPm10());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_PM25,airData.getPm25());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_NO2,airData.getNo2());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_USERID,airData.getUserID());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION,airData.getLocation());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_DATETIME,airData.getDatetime());
    }

}
