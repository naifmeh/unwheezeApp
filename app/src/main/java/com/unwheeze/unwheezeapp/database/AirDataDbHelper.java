package com.unwheeze.unwheezeapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 14/03/2018.
 */

public class AirDataDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "unwheeze.db";

    private String SQL_CREATE_ENTRIES = "CREATE TABLE " + AirDataContract.AirDataEntry.TABLE_NAME + " (" +
            AirDataContract.AirDataEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
            AirDataContract.AirDataEntry.COLUMN_NAME_USERID + " TEXT," +
            AirDataContract.AirDataEntry.COLUMN_NAME_DATETIME + " DATETIME," +
            AirDataContract.AirDataEntry.COLUMN_NAME_LOCATION + " TEXT, " +
            AirDataContract.AirDataEntry.COLUMN_NAME_PM10 + " FLOAT, " +
            AirDataContract.AirDataEntry.COLUMN_NAME_PM25 + " FLOAT, " +
            AirDataContract.AirDataEntry.COLUMN_NAME_NO2 + " FLOAT)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "+ AirDataContract.AirDataEntry.TABLE_NAME;

    public AirDataDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }
}
