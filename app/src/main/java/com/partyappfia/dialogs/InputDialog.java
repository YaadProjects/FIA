package com.partyappfia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.partyappfia.R;

/**
 * Created by Great Summit on 11/14/2015.
 */
public class InputDialog extends Dialog {

    private onOKButtonListener listener = null;
    public interface onOKButtonListener {
        public void onOKButton(String szInputed);
    }

    private EditText    evInput;
    private String      szInput = "";

    private Context     mContext;

    public InputDialog(Context context, String szInput, onOKButtonListener listener) {
        super(context);
        mContext = context;
        this.listener = listener;
        this.szInput = szInput;
    }

    public InputDialog(Context context, onOKButtonListener listener) {
        super(context);
        mContext = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_input);

        evInput = (EditText)findViewById(R.id.ev_input);
        evInput.setText(szInput);

        findViewById(R.id.lv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        findViewById(R.id.lv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                szInput = evInput.getText().toString();

                if (szInput.isEmpty()) {
                    Toast.makeText(mContext, "Please enter phone number", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!szInput.contains("+")) {
                    Toast.makeText(mContext, "Please enter the country code", Toast.LENGTH_LONG).show();
                    return;
                }

                listener.onOKButton(szInput);
                dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    public void setInputString(String Input) {
        szInput = Input;
    }

    public String getInputString() {
        return szInput;
    }
}
