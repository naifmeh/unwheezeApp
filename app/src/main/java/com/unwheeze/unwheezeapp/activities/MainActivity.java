package com.unwheeze.unwheezeapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.beans.NearestBean;
import com.unwheeze.unwheezeapp.bluetooth.BluetoothActions;
import com.unwheeze.unwheezeapp.bluetooth.MyBluetoothGattCallback;
import com.unwheeze.unwheezeapp.bluetooth.RequestDeviceAirDataTask;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.fragments.MeasureDialogFragment;
import com.unwheeze.unwheezeapp.fragments.SampleDialogFragment;
import com.unwheeze.unwheezeapp.network.AirDataLoader;
import com.unwheeze.unwheezeapp.network.NetworkUtils;
import com.unwheeze.unwheezeapp.services.WebsocketService;
import com.unwheeze.unwheezeapp.ui.ErrorDialog;
import com.unwheeze.unwheezeapp.ui.WarningDialog;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//TODO: Avant d'enregistrer toute donnee dans la db, verifier qu'elle n'y est pas deja

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String>, BluetoothActions,
        MeasureDialogFragment.MeasureDialogFragmentCallback, SampleDialogFragment.SampleDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FloatingActionButton mConnectFab;
    private FloatingActionButton mMeasureFabButton;
    private FloatingActionButton mPeriodicMeasureFab;

    private boolean isPeriodicallyScanning = false;
    private ScheduledExecutorService mReadScheduler;

    private ErrorDialog mErrorDialog;
    private WarningDialog mWarningDialog;
    private BottomSheetDialogFragment bottomSheetDialogFragment;
    private boolean isBottomSheetDisplayed = false;

    private SharedPreferences mSharedPrefs;

    private GoogleMap mainMap;
    private boolean isLocationApiPresent = true;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private List<WeightedLatLng> mHeatMapList = new ArrayList<>();
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private List<Marker> mMarkerList = new ArrayList<>();

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
    private static final int SCAN_PERIOD = 2000;

    private float mPm1 = 0.f;
    private float mPm25 = 0.f;
    private float mPm10 = 0.f;
    private char MEASURE_TRIGGER = 'B';
    private int mOverallAirOpinion = AirDataUtils.AIR_QUALITY_NEUTRAL;

    private Handler mHandler; //TODO: Handle service here
    private BroadcastReceiver mBroadcastReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private AirDataDbHelper mDbHelper = new AirDataDbHelper(this);
    private SQLiteDatabase mDb;

    private String mAirDataIdClicked;
    private boolean hasLoaderFinishedOnce = false;

    private final String FONT_PATH = "fonts/Kollektif-Bold.ttf";

    public static final int LOADER_AIRDATA_TAG =  654548;
    private static final int REQUEST_ENABLE_BT = 62328;
    private static final int REQUEST_ENABLE_LOCATION = 58976;

    public static final int PERIOD_SCHEDULED_SCAN = 30;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDb = mDbHelper.getWritableDatabase();
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
        mToolbar.setNavigationOnClickListener((view)->showWarningDialog(getString(R.string.warningNotImplemented)));

        mConnectFab = (FloatingActionButton) findViewById(R.id.fab_menu_btConnect);
        mMeasureFabButton = (FloatingActionButton) findViewById(R.id.fab_menu_measure);
        mPeriodicMeasureFab = (FloatingActionButton) findViewById(R.id.fab_menu_periodicMeasure);

        //----- Starting Loader
        getSupportLoaderManager().restartLoader(LOADER_AIRDATA_TAG,null,this).forceLoad();

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

        //---------- Bottom Sheet
        bottomSheetDialogFragment = MeasureDialogFragment.newInstance();


        //Starting location manager
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //------ Bluetooth Handling

        mConnectFab.setOnClickListener((view)-> {

            if(!mScanning) {
                if(isBtPresent) {
                    if(mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
                        startScan();
                        Log.d(TAG, "Scanning...");
                    } else {
                        Log.d(TAG,"Disconnecting");
                        mBtGatt.disconnect();
                    }
                }
            }


        });

        mMeasureFabButton.setOnClickListener((view)->writeCharacteristic('B'));

        mPeriodicMeasureFab.setOnClickListener((view)->{
            if(!isPeriodicallyScanning) {
                readPeriodically();
                readPeriodically();

            } else {
                stopPeriodicReading();
                stopPeriodicReading();

            }

        });

        //--------- Service start
        Intent serviceIntent = new Intent(this,WebsocketService.class);
        startService(serviceIntent);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG,"Result code by BTE : "+resultCode);

        } else if(requestCode == REQUEST_ENABLE_LOCATION) {
            Log.d(TAG,"Result code by Location");
        }
    }

    @Override
    protected void onPause() {
        stopLocationRequest();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!hasBtFeature()) isBtPresent = false;
        if(isBtPresent) initializeBt();

        mBroadcastReceiver = new WebSocketBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.websocketDataReceivedIntent));
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,filter);
        setLocationUpdater();


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
            requestLocationEnable();
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),REQUEST_ENABLE_LOCATION);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
                mErrorDialog = new ErrorDialog(MainActivity.this,getString(R.string.scanFailedError));
                mErrorDialog.show();
            }
        };

        Log.d(TAG,"Starting SCAN callbacks");


        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters,settings,mScanCallback);

        mScanning = true;
        mConnectFab.setLabelText(getResources().getString(R.string.scanningBte));
        mHandler = new Handler();
        mHandler.postDelayed(()-> stopScan(),SCAN_PERIOD);

    }

    private void stopScan() {

        if(mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null){
            Log.d(TAG,"Scan over");
            mBluetoothLeScanner.stopScan(mScanCallback);
            if(mBtDevices.isEmpty()) {
                //showErrorDialog(getString(R.string.scanFailedError));
            }


        }

        mScanning =false;
        onScanResult();
    }

    private void onScanResult() {
        mBtGattCallback = new MyBluetoothGattCallback(this);

        for(BluetoothDevice device : mBtDevices) {
            if(device.getName() == null) continue;
            if(device.getName().equals(getResources().getString(R.string.bluetoothDeviceName))) {
             mBtGatt = device.connectGatt(this,true,mBtGattCallback);
             Log.d(TAG,"Connected to device : "+device.getName());
             break;
            }

        }
        if(mBtGatt != null){
            mBtGatt.discoverServices();
        } else {
            mConnectFab.setLabelText(getResources().getString(R.string.connectToDeviceFab));
            mConnectFab.setClickable(true);
            mConnectFab.setColorNormal(getResources().getColor(R.color.colorPrimaryDark));
            showErrorDialog(getString(R.string.scanFailedError));
        }
    }

    private void writeCharacteristic(int value) {
        if(mBluetoothAdapter == null || mBtGatt == null) {
            Log.w(TAG,"BluetoothAdapter not init");
            showErrorDialog(getString(R.string.btNotInitError));
            return;
        }
        UUID uuid = UUID.fromString(getString(R.string.serviceBtUuid));
        BluetoothGattService bluetoothGattService = mBtGatt.getService(uuid);
        if(bluetoothGattService == null) {
            Log.w(TAG,"Writing : ble service not found");
            return;
        }

        UUID uuidWrite = UUID.fromString(getString(R.string.characWriteBtUuid));
        BluetoothGattCharacteristic mWriteCharacs = bluetoothGattService.getCharacteristic(uuidWrite);
        mWriteCharacs.setValue(value,BluetoothGattCharacteristic.FORMAT_UINT8,0);
        if(mWriteCharacs != null && mBtGatt.writeCharacteristic(mWriteCharacs) == true) {
            Log.i(TAG,"Sent data : "+value);
            //TODO: Handle UI ?
        } else Log.e(TAG,"Error while sending data");

    }

    private void readPeriodically() {
        if(mBluetoothAdapter == null || mBtGatt == null) {
            showErrorDialog(getString(R.string.btNotInitError));
            return;
        }
        mPeriodicMeasureFab.setLabelText(getString(R.string.periodicallyScanning));
        mPeriodicMeasureFab.setColorNormal(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary));
        isPeriodicallyScanning = true;
        mReadScheduler = Executors.newScheduledThreadPool(1);
        final Runnable periodicBt = ()->{
            if(bottomSheetDialogFragment != null || isBottomSheetDisplayed)
                bottomSheetDialogFragment.dismiss();
            runOnUiThread(()->writeCharacteristic(MEASURE_TRIGGER));
        };

        mReadScheduler.scheduleAtFixedRate(periodicBt,0,PERIOD_SCHEDULED_SCAN, TimeUnit.SECONDS);

    }

    private void stopPeriodicReading() {
        if(mReadScheduler != null)
            mReadScheduler.shutdown();
        mPeriodicMeasureFab.setLabelText(getString(R.string.scheduledScan));
        mPeriodicMeasureFab.setColorNormal(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryDark));
        isPeriodicallyScanning = false;
    }

    private void subscribeCharacteristic(boolean enable) {
        if(mBluetoothAdapter == null || mBtGatt == null) {
            Log.w(TAG,"BluetoothAdapter not init");
            showErrorDialog(getString(R.string.btNotInitError));
            return;
        }
        UUID uuid = UUID.fromString(getString(R.string.serviceBtUuid));
        BluetoothGattService bluetoothGattService = mBtGatt.getService(uuid);
        if(bluetoothGattService == null) {
            Log.w(TAG,"Subscribing : ble service not found");
            return;
        }

        UUID uuidRead = UUID.fromString(getString(R.string.characReadBtUuid));
        BluetoothGattCharacteristic mReadCharacs = bluetoothGattService.getCharacteristic(uuidRead);

        mBtGatt.setCharacteristicNotification(mReadCharacs,enable);
        BluetoothGattDescriptor descriptor = mReadCharacs.getDescriptor(UUID.fromString(getString(R.string.descriptorReadBtUid)));

        if(descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBtGatt.writeDescriptor(descriptor);
        } else {
            Log.w(TAG,"Null descriptor");
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
        switch(item.getItemId()) {
            case R.id.main_toolbar_refresh:
                getSupportLoaderManager().restartLoader(LOADER_AIRDATA_TAG,null,this).forceLoad();
                return true;

            case R.id.main_toolbar_profile:
                showWarningDialog(getString(R.string.warningNotImplemented));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //------------------- MAP

    @Override
    @SuppressWarnings({"ResourceType"})
    public void onMapReady(GoogleMap googleMap) {
        mainMap = googleMap;
        mainMap.setMyLocationEnabled(true);

        if(mSharedPrefs != null && mSharedPrefs.contains(getString(R.string.shared_prefs_file_current_coarse_latitude))) {
            Double longitude = Double.longBitsToDouble(mSharedPrefs.getLong(getString(R.string.shared_prefs_file_current_coarse_longitude),0));
            Double latitude = Double.longBitsToDouble(mSharedPrefs.getLong(getString(R.string.shared_prefs_file_current_coarse_latitude),0));

            LatLng loc = new LatLng(latitude,longitude);
            mainMap.setMyLocationEnabled(true);
            if(mSharedPrefs.getString(getString(R.string.shared_prefs_file_current_coarse_location),getString(R.string.countryName)).equals(getString(R.string.countryName)))
                mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 5));
            else
                mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12));
        }

        final SampleDialogFragment bottomSheetDetailFragment = SampleDialogFragment.newInstance();
        mainMap.setOnMapClickListener((latlng)->{
            Log.d(TAG,"Point cliqué : "+latlng.latitude+" , "+latlng.longitude);

            NetworkUtils networkUtils = new NetworkUtils(this);
            networkUtils.getNearest(latlng.latitude,latlng.longitude,"1",(jsonArray)->{
                Log.d(TAG,"GET NEAREST JSON ARRAY "+ jsonArray.toString());
                if(jsonArray.size() == 0) return;
                JsonElement firstElement = jsonArray.get(0);
                NearestBean nearestBean = (new Gson()).fromJson(firstElement.toString(),NearestBean.class);
                AirData airData = nearestBean.getDoc();
                mAirDataIdClicked = airData.getId();

                bottomSheetDetailFragment.show(getSupportFragmentManager(),bottomSheetDetailFragment.getTag());
            });
        });

        mainMap.setOnMarkerClickListener(marker -> {
            marker.setAlpha(1.f);
            marker.setZIndex(1.f);
            marker.showInfoWindow();
            mAirDataIdClicked = (String) marker.getTag();

            bottomSheetDetailFragment.show(getSupportFragmentManager(),bottomSheetDetailFragment.getTag());
            return true;
        });

    }

    private MarkerOptions customizeMarker(AirData airData) {
        /* Splitting position */
        String[] position = airData.getLocation().split(",");
        LatLng latlng = new LatLng(Float.parseFloat(position[0]),Float.parseFloat(position[1]));

        /* Marker options*/
        MarkerOptions markerOptions = new MarkerOptions().position(latlng);
        markerOptions.title("PM1: "+airData.getPm1()+"- PM10: "+airData.getPm10()+"- PM25: "+airData.getPm25());
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.alpha(0.8f);


        return markerOptions;
    }

    private void loadMapData() {
        if(mainMap != null) removeMarkers();
        mHeatMapList = readItems();
        if(!hasLoaderFinishedOnce && mainMap != null) {
            if(mHeatMapList.size()>0) {
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(mHeatMapList)
                        .build();

                mOverlay = mainMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                hasLoaderFinishedOnce = true;
            } else {
                showErrorDialog(getString(R.string.problemRetrievingError));
            }
        }
        else {
            mProvider.setWeightedData(mHeatMapList);
            mOverlay.clearTileCache();
        }
    }

   private ArrayList<WeightedLatLng> readItems() { //PROBLEME ICI
        //TODO filter by city
       AirDataDbHelper mDbHelper = new AirDataDbHelper(this);
       SQLiteDatabase db = mDbHelper.getReadableDatabase();

       ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();

       List<AirData> airDataList = AirDataUtils.getAirDataList(db);
       for(AirData airData: airDataList) {
           String location = airData.getLocation();
           Log.d(TAG,location);
           if(location == null) continue;

           String[] position = location.split(",");
           LatLng latlng = new LatLng(Float.parseFloat(position[0]),Float.parseFloat(position[1]));
           list.add(new WeightedLatLng(latlng,airData.getPm25()));
           /* Setting markers */
           addMarker(airData);

       }

       return list;
   }

   private void addMarker(AirData airData) {
        /* Setting markers */
       if(mainMap == null) return;
       Marker marker = mainMap.addMarker(customizeMarker(airData));
       mMarkerList.add(marker);
       marker.setTag(airData.getId());
   }

   private void removeMarkers() {
        for(Marker marker : mMarkerList) {
            marker.remove();
        }
        mMarkerList.clear();
   }



    //---------------- LOADER CALLBACKS
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"Creating loader");
        return new AirDataLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(mHandler == null) mHandler = new Handler();
        mHandler.postDelayed(()->loadMapData(),500);
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
        if(action == MyBluetoothGattCallback.ACTION_BTE_AVAILABLE) {
            Log.i(TAG,"Received some data");
            final byte[] data = characteristic.getValue();
            if(data!=null && data.length >0) {

                String[] finalValue = (new String(data)).split("-");
                Log.d(TAG,"receivedCharacteristics Value : "+new String(data));
                if(finalValue.length >= 3) {
                    mPm1 = Float.parseFloat(finalValue[0]);
                    mPm25 = Float.parseFloat(finalValue[1]);
                    mPm10 = Float.parseFloat(finalValue[2]);
                }
                if(mPm1 == 0 && mPm10 == 0 && mPm25 == 0) return;

                UUID uuid = UUID.randomUUID();
                AirData airData = new AirData(uuid.toString(),null,mPm25,mPm10,mPm1,null,null);
                mOverallAirOpinion = AirDataUtils.computeAirQuality(airData);

                setUpRequestDeviceTask(airData);

                if(isBottomSheetDisplayed != true)
                    bottomSheetDialogFragment.show(getSupportFragmentManager(),bottomSheetDialogFragment.getTag());
                else {
                    bottomSheetDialogFragment.dismiss();
                    bottomSheetDialogFragment.show(getSupportFragmentManager(),bottomSheetDialogFragment.getTag());
                }

            }
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void setLocationUpdater() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };

        mFusedLocationClient.requestLocationUpdates(locationRequest,mLocationCallback,null);

    }

    private void stopLocationRequest() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void setUpRequestDeviceTask(AirData airData) {
        AirData airDataToAdd = null;
        RequestDeviceAirDataTask requestDeviceAirDataTask = new RequestDeviceAirDataTask(this);
        setAirDataForUpload(airData);
       /*if(airDataToAdd.getLocation() != null) {
            Log.d(TAG, "Airdata to add : " + new Gson().toJson(airDataToAdd));
            requestDeviceAirDataTask.execute(airDataToAdd);
        } else {
            showErrorDialog(getString(R.string.locationNullError));
        }*/
    }

    private void setAirDataForUpload(AirData airdata) throws SecurityException{
        AirData modifiedAirData = airdata;
        if(!areLocationServicesEnabled(this)){
            Log.i(TAG,"Location services disabled in setAirDataForUpload");
            requestLocationEnable();
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d(TAG,"Getting last location Success in setAirDataForUpload");
                if(location == null) {
                    modifiedAirData.setLocation(null);
                    if(mHandler != null) {
                        mHandler.post(()->showErrorDialog(getString(R.string.locationNullError)));
                    }
                    return;
                }
                else {
                    modifiedAirData.setLocation(String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));
                    UUID uuid = UUID.randomUUID();
                    modifiedAirData.setId(uuid.toString());
                    RequestDeviceAirDataTask requestDeviceAirDataTask = new RequestDeviceAirDataTask(MainActivity.this);
                    requestDeviceAirDataTask.execute(modifiedAirData);
                }

            }


        });
        Log.d(TAG,(new Gson()).toJson(modifiedAirData));
        return;
    }




    @Override
    public void receivedUpdate(String action) {
        if(action == MyBluetoothGattCallback.ACTION_BTE_CONNECTED) {
           Log.i(TAG,"Connected to device");
           mConnectionState = BluetoothProfile.STATE_CONNECTED;
          runOnUiThread(() -> {
              mConnectFab.setLabelText(getResources().getString(R.string.connectedBteFabLabel)+" "+mBtGatt.getDevice().getName());
              mConnectFab.setColorNormal(getResources().getColor(R.color.bluetoothBlue));
          });
        } else if(action == MyBluetoothGattCallback.ACTION_BTE_DISCONNECTED){
            if(mBtGatt != null) mBtGatt.close();
            if(isPeriodicallyScanning) stopPeriodicReading();
            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
            runOnUiThread(() -> {
                mConnectFab.setLabelText(getResources().getString(R.string.connectToDeviceFab));
                mConnectFab.setColorNormal(getResources().getColor(R.color.colorPrimaryDark));
            });
        } else if(action == MyBluetoothGattCallback.ACTION_BTE_CHARACS_DISCOVERED) {
            Log.d(TAG,"Characteristics discovered");
            subscribeCharacteristic(true);
        }
    }
    //------------------------------------

    private void showErrorDialog(String message) {
        if(mErrorDialog != null && mErrorDialog.isShowing()) mErrorDialog.dismiss();
        mErrorDialog = new ErrorDialog(MainActivity.this,message);
        mErrorDialog.show();
    }

    private void showWarningDialog(String message) {
        if(mWarningDialog != null && mWarningDialog.isShowing()) mWarningDialog.dismiss();
        mWarningDialog = new WarningDialog(MainActivity.this,message);
        mWarningDialog.show();
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        mHandler = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(isPeriodicallyScanning) stopPeriodicReading();
        if(mBtGatt != null) mBtGatt.close();
        Intent stopService = new Intent(this,WebsocketService.class);
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        stopService(stopService);
        mDb.close();

        super.onDestroy();
    }

    @Override
    public float listenPm1() {
        return mPm1;
    }
    @Override
    public float listenPm25() {
        return mPm25;
    }
    @Override
    public float listenPm10() {
        return mPm10;
    }

    @Override
    public int getEmojiFace() {
        return mOverallAirOpinion;
    }

    @Override
    public void requestNewMeasure(MeasureDialogFragment fragment) {
        fragment.dismiss();
        writeCharacteristic(MEASURE_TRIGGER);
    }

    @Override
    public void isFragmentDisplayed(boolean value) {
        isBottomSheetDisplayed = value;
    }

  /* Sample Dialog fragment listener */

    @Override
    public String getAirDataId() {
        return mAirDataIdClicked;
    }

    public class WebSocketBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"wsBroadcastReceiver");
            if(intent.hasExtra(WebsocketService.AIR_DATA_WSINTENT_KEY)) {
                String airDataJson = intent.getStringExtra(WebsocketService.AIR_DATA_WSINTENT_KEY);
                AirData airData = new Gson().fromJson(airDataJson, AirData.class);
                if(airData.getId()==null) return; //TODO: Handle deletion server side
                Log.d(TAG, airDataJson);
                String[] location = {"", ""};
                if (airData != null) location = airData.getLocation().split(",");

                mDb.execSQL(AirDataUtils.insertDataSql(airData));

                if (location.length > 1) {
                    LatLng latlng = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
                    mHeatMapList.add(new WeightedLatLng(latlng, AirDataUtils.computeAQI(airData)));
                    addMarker(airData);
                }

                mProvider.setWeightedData(mHeatMapList);
                mOverlay.clearTileCache();
            } else return;
        }
    }
}
