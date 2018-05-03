package com.unwheeze.unwheezeapp.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.activities.AirDetailsActivity;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.database.AirDataDbHelper;
import com.unwheeze.unwheezeapp.network.NetworkUtils;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SampleDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = SampleDialogFragment.class.getSimpleName();

    /* Listeners */
    private SampleDialogListener mListener;
    /* UI */
    private CoordinatorLayout mBaseCoordinator;
    private TextView mPm1Value;
    private TextView mPm10Value;
    private TextView mPm25Value;
    private TextView mLocationText;
    private TextView mTimePostText;
    private TextView mVerboseResult;
    private ProgressBar mPm1Progress;
    private ProgressBar mPm10Progress;
    private ProgressBar mPm25Progress;
    private ImageView mMoreDetailImage;
    private ImageView mQuitImage;
    private ImageView mEmojiFace;

    /* Bottom Sheet Callback */
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    /* Required empty constructor */
    public SampleDialogFragment() {}

    /* Instance retrieval */
    public static SampleDialogFragment newInstance() {
        SampleDialogFragment fragment = new SampleDialogFragment();
        return fragment;
    }

    /* Executed before onCreate */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof SampleDialogListener) {
            mListener = (SampleDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()+ "must implement SampleDialogListener");
        }
    }

    /* Setting up the fragment */
    @Override
    @SuppressLint("RestrictedApi")
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_sample_details,null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)((View)contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;

        if(behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetCallback);
        }

        /* TextViews */
        mPm1Value = (TextView) contentView.findViewById(R.id.no2ValueSampleDetails);
        mPm10Value = (TextView) contentView.findViewById(R.id.pm10ValueSampleDetails);
        mPm25Value = (TextView) contentView.findViewById(R.id.pm25ValueSampleDetails);
        mLocationText = (TextView) contentView.findViewById(R.id.sampleDetailsLocationText);
        mTimePostText = (TextView) contentView.findViewById(R.id.sampleDetailsTimeText);
        mVerboseResult = (TextView) contentView.findViewById(R.id.resultVerboseSampleDetails);
        /* Progress bars */
        mPm1Progress = (ProgressBar) contentView.findViewById(R.id.no2ProgressBarSampleDetails);
        mPm10Progress = (ProgressBar) contentView.findViewById(R.id.pm10ProgressBarSampleDetails);
        mPm25Progress = (ProgressBar) contentView.findViewById(R.id.pm25ProgressBarSampleDetails);
        /* Image views */
        mMoreDetailImage = (ImageView) contentView.findViewById(R.id.sampleDetailsSeeMoreBtn);
        mQuitImage = (ImageView) contentView.findViewById(R.id.sampleDetailsQuitBtn);
        mEmojiFace = (ImageView) contentView.findViewById(R.id.resultEmojiSampleDetails);

        /* Setting values */
        String airDataId = mListener.getAirDataId();
        Log.d(TAG,airDataId);
        AirData airData = loadAirDataValues(airDataId);
        Handler handler = new Handler();
        if(airData != null) {
            mPm1Value.setText(Float.toString(airData.getPm1()));
            AirDataUtils.animateProgressBar(mPm1Progress,(int)airData.getPm1());
            mPm10Value.setText(Float.toString(airData.getPm10()));
            AirDataUtils.animateProgressBar(mPm10Progress,(int)airData.getPm10());
            mPm25Value.setText(Float.toString(airData.getPm25()));
            AirDataUtils.animateProgressBar(mPm25Progress,(int)airData.getPm25());
            mTimePostText.setText(AirDataUtils.formatDateTime(airData.getDatetime()));
            AirDataUtils.setEmojiFace(airData,getContext(),mEmojiFace,mVerboseResult);
            mLocationText.setText("");
            Thread threadName = new Thread(()->{
                String geolocation = AirDataUtils.getLocalityFromLocation(getContext(),airData.getLocation());
                handler.post(()-> mLocationText.setText(geolocation));
            });
            threadName.start();
        }

        /* Setting actions */
        mQuitImage.setOnClickListener((view)->dismiss());

        mMoreDetailImage.setOnClickListener((view)->{
            NetworkUtils networkUtils = new NetworkUtils(getActivity());
            networkUtils.getAllAirDataElements((jsonArray)->{
                Intent intent = new Intent(getActivity(),AirDetailsActivity.class);
                intent.putExtra(AirDetailsActivity.JSONARRAY_DATA_INTENT_KEY,jsonArray.toString());
                intent.putExtra(AirDetailsActivity.JSON_DATA_INTENT_KEY,(new Gson()).toJson(airData));
                startActivity(intent);
            });
        });

    }

    private AirData loadAirDataValues(String id) {
        AirDataDbHelper dbHelper = new AirDataDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        AirData airData = AirDataUtils.getAirDataById(id,db);

        return airData;
    }

    private void setEmojiFace(AirData airData) {
        int aqi = AirDataUtils.computeAQI(airData);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_neutral_icon_24px);;
        switch(aqi) {
            case AirDataUtils.AIR_QUALITY_GOOD:
                drawable = getResources().getDrawable(R.drawable.ic_happy_icon_24px);
                mVerboseResult.setText(getString(R.string.happyEmojiLabel));
                break;
            case AirDataUtils.AIR_QUALITY_NEUTRAL:
                mVerboseResult.setText(getString(R.string.neutralEmojiLabel));
                break;
            case AirDataUtils.AIR_QUALITY_BAD:
                drawable = getResources().getDrawable(R.drawable.ic_sad_icon_24px);
                mVerboseResult.setText(getString(R.string.sadEmojiLabel));
                break;
        }
        mEmojiFace.setImageDrawable(drawable);

    }


    public interface SampleDialogListener {
        String getAirDataId();
    }


}
