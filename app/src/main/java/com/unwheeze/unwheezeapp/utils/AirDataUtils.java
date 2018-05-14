package com.unwheeze.unwheezeapp.utils;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by User on 21/03/2018.
 */

public class AirDataUtils {
    private final static String TAG= AirDataUtils.class.getSimpleName();

    public static final int AIR_QUALITY_GOOD = 64548;
    public static final int AIR_QUALITY_NEUTRAL = 26898;
    public static final int AIR_QUALITY_BAD = 8963;

    public static int computeAirQuality (@NonNull AirData airData) {
        float pm1Value = airData.getPm1();
        if(pm1Value < 25)
            return AIR_QUALITY_GOOD;
        else if(pm1Value > 25 && pm1Value < 250)
            return AIR_QUALITY_NEUTRAL;
        else
            return AIR_QUALITY_BAD;
    }

    public static int computeAQI(@NonNull AirData airData) {
        int aqi = (int)airData.getPm1();
        if(aqi<40)
            return AIR_QUALITY_GOOD;
        else if(aqi>40 && aqi <70)   
            return AIR_QUALITY_NEUTRAL;
        else
            return AIR_QUALITY_BAD;
    }

    public static ContentValues populateContentValue(AirData airData) {
        ContentValues values = new ContentValues();
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_ID,airData.getId());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_PM10,airData.getPm10());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_PM25,airData.getPm25());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_PM1,airData.getPm1());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_USERID,airData.getUserID());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION,airData.getLocation());
        values.put(AirDataContract.AirDataEntry.COLUMN_NAME_DATETIME,airData.getDatetime());

        return values;
    }

    public static String insertDataSql(AirData airData) {
        String id = airData.getId();
        float pm10 = airData.getPm10();
        float pm25 = airData.getPm25();
        float pm1 = airData.getPm1();
        String loc = airData.getLocation();
        String dateTime = airData.getDatetime();


        return "REPLACE INTO "+ AirDataContract.AirDataEntry.TABLE_NAME + "("+
                AirDataContract.AirDataEntry.COLUMN_NAME_ID+", "+AirDataContract.AirDataEntry.COLUMN_NAME_PM10+", "+
                AirDataContract.AirDataEntry.COLUMN_NAME_PM25+", "+AirDataContract.AirDataEntry.COLUMN_NAME_PM1+", "+
                AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION+", "+AirDataContract.AirDataEntry.COLUMN_NAME_DATETIME+") VALUES ('"+
                id+"',"+pm10+", "+pm25+", "+pm1+", '"+loc+"', '"+dateTime+"');";



    }

    public static AirData getAirDataById(String id, SQLiteDatabase db) {

        AirData airData = new AirData();
        String whereClause = "id='"+id+"'";
        Log.d(TAG,"ID "+id);
        Cursor cursor = db.query(AirDataContract.AirDataEntry.TABLE_NAME,
                null,
                whereClause,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
            String location = cursor.getString(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION
            ));

            String datetime = cursor.getString(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_DATETIME
            ));

            float pm1 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM1
            ));
            float pm10 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM10
            ));
            float pm25 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM25
            ));
            airData = new AirData(id,location,pm25,pm10,pm1,datetime,null);


        db.close();
        cursor.close();
        return airData;
    }

    public static List<AirData> getAirDataList(@NonNull SQLiteDatabase db) {

        List<AirData> airDataList = new ArrayList<>();

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
            Log.d(TAG,location);
            String id = cursor.getString(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_ID
            ));

            float pm1 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM1
            ));
            float pm10 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM10
            ));
            float pm25 = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    AirDataContract.AirDataEntry.COLUMN_NAME_PM25
            ));
            if(location == null) continue;
            AirData airData = new AirData(id,location,pm25,pm10,pm1,null,null);

            airDataList.add(airData);
        }
        cursor.close();
        db.close();
        return airDataList;
    }

    public static String formatDateTime(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String format = "";
        try {
            Date dateParsed = simpleDateFormat.parse(date);
            SimpleDateFormat formatVoulu = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format = formatVoulu.format(dateParsed);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return format;
    }

    public static double[] splitLocation(String location) {
        String[] locationSplit = location.split(",");
        double[] locDouble = {Double.parseDouble(locationSplit[0]),Double.parseDouble(locationSplit[1])};
        return locDouble;
    }

    public static String getLocalityFromLocation(Context context,String geolocation) {
        Geocoder geocoder = new Geocoder(context, Locale.FRANCE);
        double[] location = AirDataUtils.splitLocation(geolocation);
        try {
            List<Address> addressList = geocoder.getFromLocation(location[0],location[1],1);
            return addressList.get(0).getLocality()+", "+addressList.get(0).getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    public static void animateProgressBar(ProgressBar progressBar, int value) {
        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0,value); // see this max value coming back here, we animate towards that value
        animation.setDuration (200); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
    }

    public static void setEmojiFace(AirData airData, Context context, ImageView emoji, TextView verbose) {
        int aqi = AirDataUtils.computeAQI(airData);
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_neutral_icon_24px);;
        switch(aqi) {
            case AirDataUtils.AIR_QUALITY_GOOD:
                drawable = context.getResources().getDrawable(R.drawable.ic_happy_icon_24px);
                verbose.setText(context.getString(R.string.happyEmojiLabel));
                break;
            case AirDataUtils.AIR_QUALITY_NEUTRAL:
                verbose.setText(context.getString(R.string.neutralEmojiLabel));
                break;
            case AirDataUtils.AIR_QUALITY_BAD:
                drawable = context.getResources().getDrawable(R.drawable.ic_sad_icon_24px);
                verbose.setText(context.getString(R.string.sadEmojiLabel));
                break;
        }
        emoji.setImageDrawable(drawable);

    }

    public static String deleteTableSql(){
        return "DELETE FROM "+ AirDataContract.AirDataEntry.TABLE_NAME;
    }

}
