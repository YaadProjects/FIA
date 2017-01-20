package com.partyappfia.utils;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.partyappfia.fragments.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Great Summit on 5/8/2016.
 */
public class KeyboardHelper {
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Fragment fragment) {
        try {
            InputMethodManager imm = (InputMethodManager) fragment.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = fragment.getView();
            if (view != null) {
                ArrayList<View> arrFocusViews = view.getFocusables(View.FOCUS_DOWN);
                if (arrFocusViews != null) {
                    for (View subView : arrFocusViews)
                        imm.hideSoftInputFromWindow(subView.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(BaseFragment fragment) {
        try {
            InputMethodManager imm = (InputMethodManager) fragment.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = fragment.getView();
            if (view != null) {
                ArrayList<View> arrFocusViews = view.getFocusables(View.FOCUS_DOWN);
                if (arrFocusViews != null) {
                    for (View subView : arrFocusViews)
                        imm.hideSoftInputFromWindow(subView.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
