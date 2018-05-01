package com.unwheeze.unwheezeapp.network;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

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
//TODO: HUGE PROBLEM CANT GET THE LOADER TO LOAD THE DATA WHYYYY
public class AirDataLoader extends AsyncTaskLoader<String> {

    private static final String TAG = AirDataLoader.class.getSimpleName();

    private AirDataDbHelper mDbHelper = new AirDataDbHelper(getContext());
    private SQLiteDatabase db = mDbHelper.getWritableDatabase();
    private SharedPreferences mSharedPrefs;

    private RequestQueue queue;
    private Context mCtx;

    private Uri mUri;

    public AirDataLoader(Context context) {
        super(context);
        mCtx = context;
        queue = AirDataRequestSingleton.getInstance(context.getApplicationContext())
            .getRequestQueue();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(RequestsScheme.HTTP_SCHEME)
                .encodedAuthority(RequestsScheme.AUTHORITY)
                .appendPath(RequestsScheme.APP_PATH)
                .appendPath(RequestsScheme.AIRDATA_PATH)
                .appendPath(RequestsScheme.AIRDATA_GETALL);

        mUri = builder.build();

        mSharedPrefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_file_key),context.MODE_PRIVATE);





    }

    @Override
    public void onCanceled(String data) {
        super.onCanceled(data);
    }

    @Override
    public String loadInBackground() {
        Log.d(TAG,"Loading in background "+mUri.toString());

        final Gson gson = new Gson();



        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, mUri.toString(), null, (response) -> {

                JsonArray arrayReps = new JsonArray();
                arrayReps.addAll(gson.fromJson(response.toString(),JsonArray.class));
                Log.d(TAG,arrayReps.toString());
                for(int i=0;i<arrayReps.size();i++) {
                        String jsonObj = arrayReps.get(i).toString();
                        ContentValues values = AirDataUtils.populateContentValue(gson.fromJson(jsonObj,AirData.class));
                        long resp = db.insert(AirDataContract.AirDataEntry.TABLE_NAME,null,values);
                        Log.d(TAG,"Insertion row : "+resp);
                }
                db.close();
        }, (error) -> Log.d(TAG,"onErrorResponse "+error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                NetworkUtils networkUtils = new NetworkUtils(getContext());
                String api_key=networkUtils.getApiKeyFromPrefs();

                params.put("X-Api-Key",api_key);

                return params;
            }
        };

        queue.add(jsonArrayRequest);
        return null;
    }




}
