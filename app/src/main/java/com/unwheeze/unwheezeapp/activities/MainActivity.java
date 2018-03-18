package com.unwheeze.unwheezeapp.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.bluetooth.BluetoothActions;
import com.unwheeze.unwheezeapp.bluetooth.MyBluetoothGattCallback;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.fragments.MeasureDialogFragment;
import com.unwheeze.unwheezeapp.network.AirDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String>, BluetoothActions,
        MeasureDialogFragment.MeasureDialogFragmentCallback{

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FloatingActionButton mConnectFab;
    private FloatingActionButton mMeasureFabButton;

    private SharedPreferences mSharedPrefs;

    private GoogleMap mainMap;
    private boolean isLocationApiPresent = true;
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean isBtPresent = true;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothDevice> mBtDevices = new ArrayList<>();;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBtGatt;
    private MyBluetoothGattCallback mBtGattCallback;
    private boolean mScanning = false;
    private boolean isDeviceConnected = false;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private ScanCallback mScanCallback;
    private static final int SCAN_PERIOD = 5000;

    private final Handler mHandler = new Handler();

    private final String FONT_PATH = "fonts/Kollektif-Bold.ttf";

    public static final int LOADER_AIRDATA_TAG =  654548;
    private static final int REQUEST_ENABLE_BT = 62328;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //------ Verifiying bundle

        if(savedInstanceState != null) {
            isLocationApiPresent = savedInstanceState.getBoolean(SplashActivity.GOOGLE_API_KEY);
        }

        //------ SETTING UI

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //Desactivating defautl

        Drawable burgerIc = ContextCompat.getDrawable(this,R.drawable.ic_view_headline_white_24px);
        mToolbar.setNavigationIcon(burgerIc);

        mConnectFab = (FloatingActionButton) findViewById(R.id.fab_menu_btConnect);
        mMeasureFabButton = (FloatingActionButton) findViewById(R.id.fab_menu_measure);



        //--- LOADING MAP
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.mainActivity_mapFragment);
        mapFragment.getMapAsync(this);
        //Loading special font
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am,FONT_PATH);
        TextView mainTitle = (TextView) findViewById(R.id.mainAppTitle);
        mainTitle.setTypeface(typeface);

        //----- SharedPrefs

        mSharedPrefs = this.getSharedPreferences(getString(R.string.shared_prefs_file_key),this.MODE_PRIVATE);

        //---- Setting up location

        //---------- Bottom Sheet

        BottomSheetDialogFragment bottomSheetDialogFragment = MeasureDialogFragment.newInstance();



        //----- Starting Loader

        getSupportLoaderManager().initLoader(LOADER_AIRDATA_TAG,null,this).forceLoad();

        //------ Bluetooth Handling

        mConnectFab.setOnClickListener((view)-> {

            if(!mScanning) {
                if(isBtPresent) {
                    if(mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
                        startScan();
                        Log.d(TAG, "Scanning...");
                    }
                }
            }


        });
        //TODO: Remove
        mMeasureFabButton.setOnClickListener((view)-> {
            bottomSheetDialogFragment.show(getSupportFragmentManager(),bottomSheetDialogFragment.getTag());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG,"Result code by BTE : "+resultCode);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!hasBtFeature()) isBtPresent = false;
        if(isBtPresent) initializeBt();
    }
    //-------- Verification

    private boolean hasBtFeature() {
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        return true;
    }

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        } else if(!areLocationServicesEnabled(this)) {
            //requestLocationEnable();
            return false;
        }
        return true;
    }
    public boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");
    }
    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 5465);
    }

    private void  requestLocationEnable() {
        Log.e(TAG,"Location not enabled");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //TODO request location enabling
    }


    //--------------- Bluethoot

    private void initializeBt() {
        if(!isBtPresent) return;
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void startScan() {
        if(!hasPermissions() || mScanning)
            return;

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                mBtDevices.add(result.getDevice());
                Log.d(TAG,"Got device with name "+result.getDevice().getName());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG,"Batch scan ready");
                for(ScanResult result : results)
                    mBtDevices.add(result.getDevice());
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.w(TAG,"Scan failed with error code "+errorCode);
            }
        };

        Log.d(TAG,"Starting SCAN callbacks");


        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters,settings,mScanCallback);

        mScanning = true;
        mConnectFab.setLabelText(getResources().getString(R.string.scanningBte));
        mHandler.postDelayed(()->{
            stopScan();
        },SCAN_PERIOD);

    }

    private void stopScan() {

        if(mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null){
            Log.d(TAG,"Scan over");
            mBluetoothLeScanner.stopScan(mScanCallback);
            if(mBtDevices.isEmpty())
                Log.i(TAG,"BT list is empty"); //TODO : Handle no devices found

        }

        mScanning =false;
        onScanResult();
    }

    private void onScanResult() {
        mBtGattCallback = new MyBluetoothGattCallback(this);
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
        for(BluetoothDevice device : mBtDevices) {
            if(device.getName() == null) continue;
            if(device.getName().equals(getResources().getString(R.string.bluetoothDeviceName))) {
             mBtGatt = device.connectGatt(this,true,mBtGattCallback);
             Log.d(TAG,"Connected to device : "+device.getName());
             break;
            }

        }

    }


    //---------------- Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //------------------- MAP

    @Override
    @SuppressWarnings({"ResourceType"})
    public void onMapReady(GoogleMap googleMap) {
        mainMap = googleMap;
        List<WeightedLatLng> list = readItems();

        if(!list.isEmpty()) {
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(list)
                    .build();
             mainMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } else {
            Toast.makeText(this,"Couldn't load data",Toast.LENGTH_LONG).show();
        }

        if(mSharedPrefs != null && mSharedPrefs.contains(getString(R.string.shared_prefs_file_current_coarse_latitude))) {
            Double longitude = Double.longBitsToDouble(mSharedPrefs.getLong(getString(R.string.shared_prefs_file_current_coarse_longitude),0));
            Double latitude = Double.longBitsToDouble(mSharedPrefs.getLong(getString(R.string.shared_prefs_file_current_coarse_latitude),0));

            LatLng loc = new LatLng(latitude,longitude);
            mainMap.setMyLocationEnabled(true);
            mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

        } else {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());

        }



    }

   private ArrayList<WeightedLatLng> readItems() {
        //TODO filter by city
       AirDataDbHelper mDbHelper = new AirDataDbHelper(this);
       SQLiteDatabase db = mDbHelper.getWritableDatabase();

       ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();

       String[] projection = {
               AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION,
               AirDataContract.AirDataEntry.COLUMN_NAME_PM25,
               AirDataContract.AirDataEntry.COLUMN_NAME_PM10,
               AirDataContract.AirDataEntry.COLUMN_NAME_NO2
       };

       //TODO : Maybe on a different thread ?
       Cursor cursor = db.query(
               AirDataContract.AirDataEntry.TABLE_NAME,
               null,
               null,
               null,
               null,
               null,
               null);

       while(cursor.moveToNext()) {
           String location = cursor.getString(cursor.getColumnIndexOrThrow(
                   AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION
           ));
           if(location == null) continue;
           String[] position = location.split(",");

           LatLng latlng = new LatLng(Float.parseFloat(position[0]),Float.parseFloat(position[1]));

           list.add(new WeightedLatLng(latlng,cursor.getFloat(
                   cursor.getColumnIndexOrThrow(
                           AirDataContract.AirDataEntry.COLUMN_NAME_PM25)))); //TODO: Algorithm to show overall data
       }

       return list;
   }

    //---------------- LOADER CALLBACKS
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"Creating loader");
        return new AirDataLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }


    //---------------------- BluetoothActions
    @Override
    public void setConnectionState(int connectionState) {
        this.mConnectionState = connectionState;
    }

    @Override
    public void receivedCharacteristics(String action, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG,"received");
        if(action == MyBluetoothGattCallback.ACTION_BTE_AVAILABLE) {
            Log.i(TAG,"Received some data");

        }
    }

    @Override
    public void receivedUpdate(String action) {
        if(action == MyBluetoothGattCallback.ACTION_BTE_CONNECTED) {
           Log.i(TAG,"Connected to device");
           mConnectionState = BluetoothProfile.STATE_CONNECTED;
          runOnUiThread(() -> {
              mConnectFab.setLabelText(getResources().getString(R.string.connectedBteFabLabel)+" "+mBtGatt.getDevice().getName());
              mConnectFab.setClickable(false);
              mConnectFab.setColorNormal(getResources().getColor(R.color.bluetoothBlue));
          });
        }
    }
    //------------------------------------

    @Override
    protected void onDestroy() {
        mBtGatt.disconnect();
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
