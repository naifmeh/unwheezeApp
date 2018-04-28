package com.unwheeze.unwheezeapp.utils;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataContract;

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

    public static String deleteTableSql(){
        return "DELETE FROM "+ AirDataContract.AirDataEntry.TABLE_NAME+"; VACUUM;";
    }

}
