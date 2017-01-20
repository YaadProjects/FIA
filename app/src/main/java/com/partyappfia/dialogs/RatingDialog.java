package com.partyappfia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.config.Constants;

/**
 * Created by Great Summit on 11/14/2015.
 */
public class RatingDialog extends Dialog {

    onOKButtonListener listener = null;
    public interface onOKButtonListener {
        public void onOKButton(int rate);
    }

    private RadioGroup  rgRating;
    private TextView    tvRating;

    public RatingDialog(Context context, onOKButtonListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_rating);

        rgRating = (RadioGroup) findViewById(R.id.rg_rating);
        tvRating = (TextView) findViewById(R.id.tv_rating);

        rgRating.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rdo_low:      tvRating.setText("(Low)");      break;
                    case R.id.rdo_middle:   tvRating.setText("(Middle)");   break;
                    case R.id.rdo_high:     tvRating.setText("(High)");     break;
                }
            }
        });

        findViewById(R.id.lv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        findViewById(R.id.lv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    switch (rgRating.getCheckedRadioButtonId()) {
                        case R.id.rdo_low:
                            listener.onOKButton(Constants.LEVEL_LOW);
                            break;
                        case R.id.rdo_middle:
                            listener.onOKButton(Constants.LEVEL_MIDDLE);
                            break;
                        case R.id.rdo_high:
                            listener.onOKButton(Constants.LEVEL_HIGH);
                            break;
                        default:
                            listener.onOKButton(Constants.LEVEL_NONE);
                            break;
                    }
                }

                dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancel();
    }
}
