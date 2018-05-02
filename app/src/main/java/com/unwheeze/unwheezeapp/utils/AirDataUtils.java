package com.unwheeze.unwheezeapp.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 21/03/2018.
 */

public class AirDataUtils {

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
        return (int)airData.getPm1();
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

    public static List<AirData> getAirDataList(@NonNull SQLiteDatabase db) {
        String[] projection = {
                AirDataContract.AirDataEntry.COLUMN_NAME_ID,
                AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION,
                AirDataContract.AirDataEntry.COLUMN_NAME_PM25,
                AirDataContract.AirDataEntry.COLUMN_NAME_PM10,
                AirDataContract.AirDataEntry.COLUMN_NAME_PM1
        };

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
            AirData airData = new AirData(id,location,pm1,pm10,pm25,null,null);

            airDataList.add(airData);
        }

        return airDataList;
    }

    public static String deleteTableSql(){
        return "DELETE FROM "+ AirDataContract.AirDataEntry.TABLE_NAME+"; VACUUM;";
    }

}
