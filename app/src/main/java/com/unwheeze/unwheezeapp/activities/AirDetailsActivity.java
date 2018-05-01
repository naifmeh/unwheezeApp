package com.unwheeze.unwheezeapp.activities;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.utils.DateAxisFormatter;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Activity class displaying a data graph along with useful information
 */
public class AirDetailsActivity extends AppCompatActivity {
    private static final String TAG = AirDetailsActivity.class.getSimpleName();

    public static final String JSONARRAY_DATA_INTENT_KEY = "jsonarray_intent_dataKey";

    /* --- UI --- */
    private LineChart mLineChart;
    private TextView mLocationText;

    /* --- DATA --- */
    private List<Entry> mEntryListPm1;
    private List<Entry> mEntryListPm25;
    private List<Entry> mEntryListPm10;
    private List<AirData> mListAirData;

    /* --- THREADING --- */
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_details);

        /* Recuperating JsonArrayData and splitting*/
        JsonArray jsonArrayData = new JsonArray();
        if(getIntent().hasExtra(JSONARRAY_DATA_INTENT_KEY)) {
            jsonArrayData = new Gson().fromJson(getIntent().getStringExtra(JSONARRAY_DATA_INTENT_KEY),
                    JsonArray.class);
        }
        mListAirData = new ArrayList<>();
        mEntryListPm1 = new ArrayList<>();
        mEntryListPm10 = new ArrayList<>();
        mEntryListPm25 = new ArrayList<>();
        for(JsonElement airData : jsonArrayData) {
            mListAirData.add((new Gson()).fromJson(airData.toString(),AirData.class));
        }

        /* Instantiating UI components */
        mLineChart = (LineChart) findViewById(R.id.airDetailsChart);
        mLocationText = (TextView) findViewById(R.id.airDetailsLocationText);

        /* Initializing components */
        setLineChart();

        /* Populating chart data set */
        mHandler = new Handler();
        if(mListAirData.size() != 0)
            setDataForDisplay(mListAirData);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    /**
     * Method used to setup the chart, such as color lines, disposition, etc...
     */
    private void setLineChart() {
        mLineChart.setHighlightPerTapEnabled(true);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDrawBorders(false);
        mLineChart.animateX(500);
        mLineChart.getDescription().setText("");
    }

    /**
     * Method used to load data into the chart
     */
    private void loadDataIntoChart(long referenceTimestamp) {
        /* Setting value formatter */
        IAxisValueFormatter xAxisFormatter = new DateAxisFormatter(referenceTimestamp);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setDrawLabels(false);

        /* Setting the markerView */

        /* Setting the dataset inside the chart */
        LineDataSet dataSetPm1 = new LineDataSet(mEntryListPm1,getString(R.string.no2TitleLabel));
        dataSetPm1.setColor(ContextCompat.getColor(this,R.color.paleGreen));
        dataSetPm1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetPm1.setDrawFilled(true);
        dataSetPm1.setDrawValues(false);
        dataSetPm1.setDrawCircles(false);
        dataSetPm1.setFillAlpha(255);
        dataSetPm1.setFillColor(ContextCompat.getColor(this,R.color.paleGreen));
        LineData lineDataPm1 = new LineData(dataSetPm1);

        LineDataSet dataSetPm10 = new LineDataSet(mEntryListPm1,getString(R.string.pm10TitleLabel));
        dataSetPm10.setColor(ContextCompat.getColor(this,R.color.colorAccent));
        dataSetPm10.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetPm10.setDrawFilled(true);
        dataSetPm10.setDrawValues(false);
        dataSetPm10.setDrawCircles(false);
        dataSetPm10.setFillAlpha(255);
        dataSetPm10.setFillColor(ContextCompat.getColor(this,R.color.colorAccent));
        LineData lineDataPm10 = new LineData(dataSetPm10);

        LineDataSet dataSetPm25 = new LineDataSet(mEntryListPm1,getString(R.string.pm25TitleLabel));
        dataSetPm25.setColor(ContextCompat.getColor(this,R.color.errorRed));
        dataSetPm25.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetPm25.setDrawFilled(true);
        dataSetPm25.setDrawValues(false);
        dataSetPm25.setDrawCircles(false);
        dataSetPm25.setFillAlpha(255);
        dataSetPm25.setFillColor(ContextCompat.getColor(this,R.color.errorRed));
        LineData lineDataPm25 = new LineData(dataSetPm25);

        List<LineDataSet> listData = new ArrayList<>();
        listData.add(dataSetPm1);
        listData.add(dataSetPm10);
        listData.add(dataSetPm25);

        LineData lineData = new LineData(dataSetPm1); //TODO: Change to get all the lines

        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    /**
     * Util method to get timestamp from string date
     */
    private long convertToTimestamp(String date) throws ParseException{
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat.parse(date).getTime();
    }

    /**
     * Method used to sort and arrange the data in another thred
     */
    private void setDataForDisplay(List<AirData> listData) {
        Thread dataThread = new Thread(()->{
            Collections.sort(listData);
            try {
                long referenceTimestamp = convertToTimestamp(listData.get(0).getDatetime());
                for (AirData data : listData) {
                    /* Transforming date to timestamp */
                    long timestamp = convertToTimestamp(data.getDatetime());
                    long newTimstmp = timestamp - referenceTimestamp; //In order to reduce timestamp
                    mEntryListPm1.add(new Entry(newTimstmp, data.getPm1()));
                    mEntryListPm10.add(new Entry(newTimstmp,data.getPm10()));
                    mEntryListPm25.add(new Entry(newTimstmp,data.getPm25()));
                }
                mHandler.post(()->{
                    loadDataIntoChart(referenceTimestamp);
                });
            } catch(ParseException e) {
                
            }

        });
        dataThread.start();
    }


}
