package com.partyappfia.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Great Summit on 2/20/2016.
 */
public class BusyHelper {
    private static ProgressDialog progressDialog = null;

    public static void show(Context context) {
        show(context, null);
    }

    public static void show(Context context, String message) {
        if (progressDialog != null)
            return;
        progressDialog = ProgressDialog.show(context, null, message, true);
    }

    public static void show(Context context, String message, boolean bCancelable) {
        if (progressDialog != null)
            return;
        progressDialog = ProgressDialog.show(context, null, message, true);
        if (bCancelable)
            progressDialog.setCancelable(true);
    }

    public static void hide() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
