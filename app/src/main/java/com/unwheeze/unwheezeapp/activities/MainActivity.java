package com.unwheeze.unwheezeapp.activities;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.database.AirDataContract;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.network.AirDataLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String>{

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;

    private GoogleMap mainMap;

    private final String FONT_PATH = "fonts/Kollektif-Bold.ttf";

    public static final int LOADER_AIRDATA_TAG =  654548;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false); //Desactivating defautl

        Drawable burgerIc = ContextCompat.getDrawable(this,R.drawable.ic_view_headline_white_24px);
        mToolbar.setNavigationIcon(burgerIc);
        //--- LOADING MAP
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.mainActivity_mapFragment);
        mapFragment.getMapAsync(this);
        //Loading special font
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am,FONT_PATH);
        TextView mainTitle = (TextView) findViewById(R.id.mainAppTitle);
        mainTitle.setTypeface(typeface);

        //---- Coloring status bar

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        //----- Starting Loader

        getSupportLoaderManager().initLoader(LOADER_AIRDATA_TAG,null,this).forceLoad();
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
    public void onMapReady(GoogleMap googleMap) {
        mainMap = googleMap;
        List<WeightedLatLng> list = readItems();

        if(!list.isEmpty()) {
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(list)
                    .build();
            TileOverlay mOverlay = mainMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } else {
            Toast.makeText(this,"Couldn't load data",Toast.LENGTH_LONG).show();
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
}
