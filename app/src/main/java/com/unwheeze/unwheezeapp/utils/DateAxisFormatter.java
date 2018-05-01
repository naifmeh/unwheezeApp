package com.unwheeze.unwheezeapp.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 30/04/2018.
 */

public class DateAxisFormatter implements IAxisValueFormatter {

    private Date mDate;
    private long refTimestamp;
    private DateFormat mDateFormat;


    public DateAxisFormatter(long refTimestamp) {
        this.refTimestamp = refTimestamp;
        this.mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        this.mDate = new Date();

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long convertedTimestamp = (long) value;
        long originalTimestamp = refTimestamp + convertedTimestamp;


        return getHour(originalTimestamp);
    }



    private String getHour(long timestamp) {
        try {
            mDate.setTime(timestamp*1000);
            return mDateFormat.format(mDate);
        } catch(Exception ex) {
            return "xx";
        }
    }
}
