package com.unwheeze.unwheezeapp.beans;



public class AirData {

    private String location;
    private float pm25;
    private float pm10;
    private float no2;
    private String datetime;
    private String userID;

    public AirData() {
    }

    public AirData(String location, float pm25, float pm10, String datetime, String userID) {
        this.location = location;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.datetime = datetime;
        this.userID = userID;
    }

    public AirData(String location, float pm25, float pm10, float no2, String datetime, String userID) {
        this.location = location;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.no2 = no2;
        this.datetime = datetime;
        this.userID = userID;
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

    public float getNo2() {
        return no2;
    }

    public void setNo2(float no2) {
        this.no2 = no2;
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

    @Override
    public String toString() {
        return getLocation()+", "+getPm10();
    }


}
