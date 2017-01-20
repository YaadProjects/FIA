package com.partyappfia.model;

import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.utils.TimeHelper;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Great Summit on 7/16/2016.
 */
public class PartyItem {
    private String      id;
    private String      title;
    private String      comment;
    private String      address;
    private LatLng      latLng;
    private String      expireTime;
    private UserInfo    ownerUser;
    private ArrayList<InvitedItem> arrInvitedItems;

    public PartyItem() {
        id              = "";
        title           = "";
        comment         = "";
        address         = "";
        latLng          = null;
        expireTime      = "";
        ownerUser       = null;
        arrInvitedItems = new ArrayList<InvitedItem>();
    }

    public PartyItem(PartyItem other) {
        id              = other.id;
        title           = other.title;
        comment         = other.comment;
        address         = other.address;
        latLng          = other.latLng;
        expireTime      = other.expireTime;
        ownerUser       = other.ownerUser;
        arrInvitedItems = other.arrInvitedItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getLatLngString() {
        if (latLng == null)
            return "";

        return String.format("%f,%f", latLng.latitude, latLng.longitude);
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public String getExpireTimeByLocal() {
        if (expireTime.isEmpty())
            return "";

        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat(TimeHelper.ISO8601DATEFORMAT);
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(expireTime);

            SimpleDateFormat localFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_DISPLAY_PARTY);
            localFormat.setTimeZone(TimeZone.getDefault());
            String formattedDate = localFormat.format(date);

            return formattedDate;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return expireTime;
    }

    public void setExpireTime(String utcTime) {
        this.expireTime = utcTime;
    }

    public void setExpireTimeFromLocal(String localTime) {
        if (localTime != null || localTime.isEmpty()) {
            expireTime = "";
            return;
        }

        try {
            SimpleDateFormat localFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_DISPLAY_PARTY);
            localFormat.setTimeZone(TimeZone.getDefault());
            Date date = localFormat.parse(localTime);

            SimpleDateFormat utcFormat = new SimpleDateFormat(TimeHelper.ISO8601DATEFORMAT);
            utcFormat.setTimeZone(TimeZone.getDefault());
            expireTime = utcFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserInfo getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(UserInfo ownerUser) {
        this.ownerUser = ownerUser;
    }

    public ArrayList<InvitedItem> getInvitedItems() {
        return arrInvitedItems;
    }

    public void setInvitedItems(ArrayList<InvitedItem> arrInvitedItems) {
        this.arrInvitedItems = arrInvitedItems;
    }

    public ArrayList<String> getInvitedUserIds() {
        if (arrInvitedItems == null)
            return null;
        ArrayList<String> invitedUserIds = new ArrayList<String>();
        for (InvitedItem item : arrInvitedItems) {
            invitedUserIds.add(item.getUser().getId());
        }
        return invitedUserIds;
    }

    public ArrayList<Integer> getInvitedUsers() {
        if (arrInvitedItems == null)
            return null;
        ArrayList<Integer> invitedUserIds = new ArrayList<Integer>();
        for (InvitedItem item : arrInvitedItems) {
            invitedUserIds.add(item.getUser().getUserId());
        }
        return invitedUserIds;
    }

    public ArrayList<Integer> getInvitedStates() {
        if (arrInvitedItems == null)
            return null;
        ArrayList<Integer> invitedStates = new ArrayList<Integer>();
        for (InvitedItem item : arrInvitedItems) {
            invitedStates.add(Integer.valueOf(item.getState()));
        }
        return invitedStates;
    }

    public ArrayList<String> getInvitedCheckTimes() {
        if (arrInvitedItems == null)
            return null;
        ArrayList<String> invitedCheckTimes = new ArrayList<String>();
        for (InvitedItem item : arrInvitedItems) {
            String checkTime = item.getCheckTime();
            if (checkTime.isEmpty())
                checkTime = " ";
            invitedCheckTimes.add(checkTime);
        }
        return invitedCheckTimes;
    }

    public boolean isEqual(String id) {
        return this.id.equals(id);
    }

    public boolean isEqual(PartyItem item) {
        if (item == null)
            return false;
        return this.id.equals(item.getId());
    }

    public void clone(PartyItem other) {
        id              = other.id;
        title           = other.title;
        comment         = other.comment;
        address         = other.address;
        latLng          = other.latLng;
        expireTime      = other.expireTime;
        ownerUser       = other.ownerUser;
        arrInvitedItems = other.arrInvitedItems;
    }

    public int getMyState() {
        if (ownerUser == null)
            return Constants.PARTY_STATE_CREATED;

        if (ownerUser.isEqual(FiaApplication.getMyUserInfo()))
            return Constants.PARTY_STATE_CREATED;

        if (arrInvitedItems == null || arrInvitedItems.isEmpty())
            return Constants.PARTY_STATE_CREATED;

        for (InvitedItem item : arrInvitedItems) {
            if (item == null)
                continue;

            if (item.getUser() == null)
                continue;

            if (item.getUser().isEqual(FiaApplication.getMyUserInfo())) {
                return item.getState();
            }
        }

        return Constants.PARTY_STATE_CREATED;
    }
}
