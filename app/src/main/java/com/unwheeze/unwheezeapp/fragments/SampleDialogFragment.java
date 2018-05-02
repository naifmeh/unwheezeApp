package com.unwheeze.unwheezeapp.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;


public class SampleDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = SampleDialogFragment.class.getSimpleName();

    /* Listeners */
    private SampleDialogListener mListener;
    /* ID AirData */
    private String idAirData;
    /* UI */
    private CoordinatorLayout mBaseCoordinator;
    private TextView mPm1Value;
    private TextView mPm10Value;
    private TextView mPm25Value;
    private TextView mLocationText;
    private TextView mTimePostText;
    private ImageView mMoreDetailImage;
    private ImageView mQuitImage;

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


    }

    interface SampleDialogListener {
        String getAirDataId();
    }


}
