package com.unwheeze.unwheezeapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.unwheeze.unwheezeapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private boolean hasBTE = true;
    private boolean hasGoogleApi = false;

    public final static String BTE_KEY = "BLUETHOOT_PRESENT_KEY";
    public final static String GOOGLE_API_KEY = "GOOGLE_API_AVAILABILITY";

    private final static int REQUEST_CODE_GOOGLE_API = 68716;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = new Bundle();

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            hasBTE = false;
        }

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode;
        if((errorCode = googleApiAvailability.isGooglePlayServicesAvailable(this)) == ConnectionResult.SUCCESS) {
            hasGoogleApi = true;
        } else {
            googleApiAvailability.getErrorDialog(this,errorCode,REQUEST_CODE_GOOGLE_API);
        }


        bundle.putBoolean(BTE_KEY,hasBTE);
        bundle.putBoolean(GOOGLE_API_KEY,hasGoogleApi);

        mSharedPrefs = this.getSharedPreferences(getString(R.string.shared_prefs_file_key),this.MODE_PRIVATE);

        if(hasGoogleApi) {
            Log.d(TAG,"Getting last known location...");
            saveLastKnownLocation();
        }
        Intent intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
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
}
