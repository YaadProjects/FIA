package com.partyappfia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.partyappfia.activities.HomeActivity;

/**
 * Created by Great Summit on 10/29/2015.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public HomeActivity getHomeActivity() {
        return (HomeActivity)getActivity();
    }
}
