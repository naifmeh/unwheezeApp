package com.unwheeze.unwheezeapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.network.AirDataRequestSingleton;
import com.unwheeze.unwheezeapp.network.RequestsScheme;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    //TODO: Load in AsyncTask
    private static final String TAG = SplashActivity.class.getSimpleName();

    private boolean hasBTE = true;
    private boolean hasGoogleApi = false;

    public final static String BTE_KEY = "BLUETHOOT_PRESENT_KEY";
    public final static String GOOGLE_API_KEY = "GOOGLE_API_AVAILABILITY";

    private final static int REQUEST_CODE_GOOGLE_API = 68716;

    private SharedPreferences mSharedPrefs;

    private Bundle mainBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mainBundle = new Bundle();


       SplashAsyncTask splashAsyncTask = new SplashAsyncTask(this);
       splashAsyncTask.execute();


    }

    @SuppressWarnings({"ResourceType"})
    private void saveLastKnownLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(hasLocationPermissions()) {
            Log.d(TAG,"Location enabled, proceeding...");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, (location) -> {
                if(location != null) {
                    Geocoder gcd = new Geocoder(this, Locale.getDefault());
                    List<Address> address = null;
                    try {
                        address = gcd.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                    }
                    if(address != null && address.size()>0) {
                        Log.i(TAG,address.get(0).getLocality());
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putString(getString(R.string.shared_prefs_file_current_coarse_location), address.get(0).getLocality());
                        editor.putLong(getString(R.string.shared_prefs_file_current_coarse_latitude),Double.doubleToRawLongBits(location.getLatitude()));
                        editor.putLong(getString(R.string.shared_prefs_file_current_coarse_longitude),Double.doubleToRawLongBits(location.getLongitude()));
                        editor.commit();
                    }
                } else {
                    Geocoder gcd = new Geocoder(this,Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = gcd.getFromLocationName(getString(R.string.countryName),1);
                    } catch(IOException e) {
                        Log.e(TAG,e.getMessage());
                    }

                    if(addresses != null && addresses.size()>0) {
                        Log.i(TAG,addresses.get(0).getCountryName()!=null?addresses.get(0).getCountryName():"");
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putString(getString(R.string.shared_prefs_file_current_coarse_location), addresses.get(0).getLocality());
                        editor.putLong(getString(R.string.shared_prefs_file_current_coarse_latitude),Double.doubleToRawLongBits(addresses.get(0).getLatitude()));
                        editor.putLong(getString(R.string.shared_prefs_file_current_coarse_longitude),Double.doubleToRawLongBits(addresses.get(0).getLongitude()));
                        editor.commit();
                    }
                }

            });
        }

    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void displayErrorDialog(GoogleApiAvailability googleApiAvailability,int errorCode) {
        googleApiAvailability.getErrorDialog(this,errorCode,REQUEST_CODE_GOOGLE_API);
    }
    private void displayMainActivity() {
        Intent intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
    }


    public class SplashAsyncTask extends AsyncTask<Void,Void,Integer> {

        private RequestQueue queue;
        private Uri mUri;
        private Context mCtx;

        public SplashAsyncTask(Context context) {
            mCtx = context;
            queue = AirDataRequestSingleton.getInstance(mCtx).getRequestQueue();

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(RequestsScheme.HTTP_SCHEME)
                    .encodedAuthority(RequestsScheme.AUTHORITY)
                    .encodedPath(RequestsScheme.APP_PATH)
                    .appendPath(RequestsScheme.AUTH_PATH)
                    .appendPath(RequestsScheme.AUTH_API_TOKEN);

            mUri = builder.build();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Bundle bundle = new Bundle();
            mSharedPrefs = mCtx.getSharedPreferences(getString(R.string.shared_prefs_file_key),mCtx.MODE_PRIVATE);

            StringRequest apiKeyRequest = new StringRequest(Request.Method.GET,mUri.toString(),(response)-> {

                JsonObject jsonObject = (new Gson()).fromJson(response, JsonObject.class);
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putString(getString(R.string.shared_prefs_file_api_key), jsonObject.get("key").getAsString());
                Log.d(TAG, jsonObject.get("key").getAsString());
                editor.commit();


            }, (error) -> {});

            if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                hasBTE = false;
            }

            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int errorCode;
            if((errorCode = googleApiAvailability.isGooglePlayServicesAvailable(mCtx)) == ConnectionResult.SUCCESS) {
                hasGoogleApi = true;
            } else {
                displayErrorDialog(googleApiAvailability,errorCode);
            }


            bundle.putBoolean(BTE_KEY,hasBTE);
            bundle.putBoolean(GOOGLE_API_KEY,hasGoogleApi); //TODO: Why tf do I have a bundle



            if(hasGoogleApi) {
                Log.d(TAG,"Getting last known location...");
                saveLastKnownLocation();
            }
            Log.d(TAG,"Done asynctask");
            queue.add(apiKeyRequest);
            //TODO: Remove thread.sleep
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            Log.d(TAG,"In post execute");
            displayMainActivity();
        }


    }
}
