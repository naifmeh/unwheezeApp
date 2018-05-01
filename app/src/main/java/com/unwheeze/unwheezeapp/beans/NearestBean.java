package com.unwheeze.unwheezeapp.beans;

/**
 * Created by User on 29/04/2018.
 */

public class NearestBean {

    public float dist;
    public AirData doc;

    public NearestBean(float dist, AirData doc) {
        this.dist = dist;
        this.doc = doc;
    }

    public NearestBean() {
    }

    public float getDist() {
        return dist;
    }

    public void setDist(float dist) {
        this.dist = dist;
    }

    public AirData getDoc() {
        return doc;
    }

    public void setDoc(AirData doc) {
        this.doc = doc;
    }
}
