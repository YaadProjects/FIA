package com.partyappfia.model;

import android.content.Context;
import android.graphics.Color;

import com.partyappfia.R;
import com.partyappfia.config.Constants;

/**
 * Created by Great Summit on 7/15/2016.
 */
public class InvitedItem {
    private UserInfo    user;
    private int         state;
    private String      checkTime;

    public InvitedItem() {
        user        = null;
        state       = Constants.PARTY_STATE_INVITED;
        checkTime   = "";
    }

    public InvitedItem(UserInfo user, int state, String checkTime) {
        this.user       = user;
        this.state      = state;
        this.checkTime  = checkTime;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCheckTime() {
        if (checkTime == null)
            return "";

        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime.trim();
    }

    public String getStateString() {
        switch (state) {
            case Constants.PARTY_STATE_ACCEPTED:
                return "Accepted";
            case Constants.PARTY_STATE_REJECTED:
                return "Rejected";
        }

        return "";
    }

    public int getStateColor(Context context) {
        switch (state) {
            case Constants.PARTY_STATE_ACCEPTED:
                return context.getResources().getColor(R.color.colorBlue);
            case Constants.PARTY_STATE_REJECTED:
                return context.getResources().getColor(R.color.colorRed);
        }

        return Color.BLACK;
    }
}
