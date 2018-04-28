package com.unwheeze.unwheezeapp.beans;


import android.os.Parcelable;

import java.io.Serializable;

public class AirData {

    private String id;
    private String location;
    private float pm25;
    private float pm10;
    private float pm1;
    private String datetime;
    private String userID;

    public AirData() {
    }

    public AirData(String id,String location, float pm25, float pm10, String datetime, String userID) {
        this.location = location;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.datetime = datetime;
        this.userID = userID;
        this.id = id;
    }



    public AirData(String id, String location, float pm25, float pm10, float pm1, String datetime, String userID) {
        this.location = location;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.pm1 = pm1;
        this.datetime = datetime;
        this.userID = userID;
        this.id = id;

    }

    public AirData(String id,float pm25, float pm10, float pm1) {
        this.id = id;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.pm1 = pm1;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getPm25() {
        return pm25;
    }

    public void setPm25(float pm25) {
        this.pm25 = pm25;
    }

    public float getPm10() {
        return pm10;
    }

    public void setPm10(float pm10) {
        this.pm10 = pm10;
    }

    public float getPm1() {
        return pm1;
    }

    public void setPm1(float pm1) {
        this.pm1 = pm1;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getLocation()+", "+getPm10();
    }


}
