package com.unwheeze.unwheezeapp.database;

import android.provider.BaseColumns;

/**
 * Created by Naif Mehanna on 14/03/2018.
 */

public final class AirDataContract {
    private AirDataContract() {}

    public static class AirDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "airData";
        public static final String COLUMN_NAME_USERID = "userID";
        public static final String COLUMN_NAME_PM10 = "pm10";
        public static final String COLUMN_NAME_PM25 = "pm25";
        public static final String COLUMN_NAME_PM1 = "no2";
        public static final String COLUMN_NAME_DATETIME = "datetime";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_ID = "id";
    }
}
