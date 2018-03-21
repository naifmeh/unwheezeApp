package com.unwheeze.unwheezeapp.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.unwheeze.unwheezeapp.R;
import com.unwheeze.unwheezeapp.beans.AirData;
import com.unwheeze.unwheezeapp.utils.AirDataUtils;

import org.w3c.dom.Text;


public class MeasureDialogFragment extends BottomSheetDialogFragment {

    private ProgressBar mPm1ProgressBar;
    private ProgressBar mPm25ProgressBar;
    private ProgressBar mPm10ProgressBar;

    private TextView mPm1TextView;
    private TextView mPm25TextView;
    private TextView mPm10TextView;

    private TextView mFinalOpinion;
    private ImageView mSmiley;

    private Button mMeasureAgainBtn;

    public static final int EMOJI_HAPPY = AirDataUtils.AIR_QUALITY_GOOD;
    public static final int EMOJI_NEUTRAL = AirDataUtils.AIR_QUALITY_NEUTRAL;
    public static final int EMOJI_SAD = AirDataUtils.AIR_QUALITY_BAD;

    private MeasureDialogFragmentCallback mListener;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
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

    public MeasureDialogFragment() {
        // Required empty public constructor
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(),R.layout.fragment_sample_measure,null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;


        if(behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        mFinalOpinion = (TextView) contentView.findViewById(R.id.resultVerbose);
        mSmiley = (ImageView) contentView.findViewById(R.id.resultEmoji);

        mMeasureAgainBtn = (Button) contentView.findViewById(R.id.refreshButton);
        mMeasureAgainBtn.setOnClickListener((view) -> {
            mListener.requestNewMeasure();
        });

        //TODO: Modify this and apply for every other progressBar
        mPm1ProgressBar = (ProgressBar) contentView.findViewById(R.id.no2ProgressBar);
        mPm1TextView = (TextView) contentView.findViewById(R.id.no2Value);
        float pm1Value = mListener.listenPm1();
        mPm1TextView.setText(Integer.toString((int)pm1Value));
        animateProgressBar(mPm1ProgressBar,(int)pm1Value);

        mPm25ProgressBar = (ProgressBar) contentView.findViewById(R.id.pm25ProgressBar);
        mPm25TextView = (TextView) contentView.findViewById(R.id.pm25Value);
        float pm25Value = mListener.listenPm25();
        mPm25TextView.setText(Integer.toString((int)pm25Value));
        animateProgressBar(mPm25ProgressBar,(int)pm25Value);

        mPm10ProgressBar = (ProgressBar) contentView.findViewById(R.id.pm10ProgressBar);
        mPm10TextView = (TextView) contentView.findViewById(R.id.pm10Value);
        float pm10Value = mListener.listenPm10();
        mPm10TextView.setText(Integer.toString((int)pm10Value));
        animateProgressBar(mPm10ProgressBar,(int)pm10Value);

        setEmoji(mListener.getEmojiFace());

    }

    public static MeasureDialogFragment newInstance() {
        MeasureDialogFragment fragment = new MeasureDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener.isFragmentDisplayed(true);

    }

    private void animateProgressBar(ProgressBar progressBar,int value) {
        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0,value); // see this max value coming back here, we animate towards that value
        animation.setDuration (1000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
    }

    private void setEmoji(int quality) {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_neutral_icon_24px);;
        switch(quality) {
            case EMOJI_HAPPY:
                drawable = getResources().getDrawable(R.drawable.ic_happy_icon_24px);
                mFinalOpinion.setText(getString(R.string.happyEmojiLabel));
                break;
            case EMOJI_NEUTRAL:
                mFinalOpinion.setText(getString(R.string.neutralEmojiLabel));
                break;
            case EMOJI_SAD:
                 drawable = getResources().getDrawable(R.drawable.ic_sad_icon_24px);
                 mFinalOpinion.setText(getString(R.string.sadEmojiLabel));
                 break;
        }

        mSmiley.setImageDrawable(drawable);
    }

    public void setAirValue(AirData airData) {
        mPm1TextView.setText(Integer.toString((int)airData.getPm1()));
        mPm25TextView.setText(Integer.toString((int)airData.getPm25()));
        mPm10TextView.setText(Integer.toString((int)airData.getPm10()));
        setEmoji(mListener.getEmojiFace());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MeasureDialogFragmentCallback) {
            mListener = (MeasureDialogFragmentCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.isFragmentDisplayed(false);
        mListener = null;
    }


    public interface MeasureDialogFragmentCallback {

        float listenPm1();
        float listenPm25();
        float listenPm10();
        int getEmojiFace();
        void requestNewMeasure();
        void isFragmentDisplayed(boolean value);
    }
}
