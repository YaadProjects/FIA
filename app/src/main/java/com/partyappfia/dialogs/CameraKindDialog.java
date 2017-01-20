package com.partyappfia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.partyappfia.R;

/**
 * Created by Great Summit on 11/2/2015.
 */
public class CameraKindDialog extends Dialog {

    public static final int KIND_PHOTO = 1;
    public static final int KIND_GALLERY = 2;

    onCameraKindListener listener = null;
    public interface onCameraKindListener {
        public void onCameraKind(int kind);
    }

    public CameraKindDialog(Context context, onCameraKindListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_camera_kind);

        findViewById(R.id.lv_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onCameraKind(KIND_PHOTO);
                dismiss();
            }
        });

        findViewById(R.id.lv_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onCameraKind(KIND_GALLERY);
                dismiss();
            }
        });

        findViewById(R.id.lv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
    	cancel();
    }
}
