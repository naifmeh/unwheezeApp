package com.unwheeze.unwheezeapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.unwheeze.unwheezeapp.R;

/**
 * Created by User on 05/05/2018.
 */

public class WarningDialog extends Dialog {
    /* UI */
    private TextView mErrorText;
    private Button mActionButton;

    /* Data */
    private String mDataText;

    /* Optional click listener */
    private View.OnClickListener mBtnClickListener;

    public WarningDialog(@NonNull Context context, @NonNull String message) {
        super(context);
        mDataText = message;
    }

    public WarningDialog(@NonNull Context context,@NonNull String message,View.OnClickListener clickAction) {
        super(context);
        mDataText = message;
        mBtnClickListener = clickAction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Requesting dialog */
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /* Setting layout */
        setContentView(R.layout.warning_display_layout);

        /* Instantiating UI components */
        mErrorText = (TextView) findViewById(R.id.warningPopUpMessage);
        mActionButton = (Button) findViewById(R.id.warningOkButton);

        /* Setting up button action & text*/
        mErrorText.setText(mDataText);
        if(mBtnClickListener == null) {
            mActionButton.setOnClickListener((view)-> {
                dismiss();
            });
        } else {
            mActionButton.setOnClickListener(mBtnClickListener);
        }
    }
}
