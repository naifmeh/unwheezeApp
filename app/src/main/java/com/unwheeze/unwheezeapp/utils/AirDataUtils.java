package com.unwheeze.unwheezeapp.utils;

import android.support.annotation.NonNull;

import com.unwheeze.unwheezeapp.beans.AirData;

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
}
