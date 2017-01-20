package com.partyappfia.model;

import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Great Summit on 7/15/2016.
 */
public class UserInfo implements Serializable {
    private String  id;
    private int     userId;
    private String  username;
    private String  phone;
    private String  photoUrl;
    private ArrayList<Integer> rates;
    private ArrayList<UserInfo> friends;
    public static final int DEVICE_ANDROID      = 0;
    public static final int DEVICE_IPHONE       = 1;
    private int     deviceType;     // 0:android, 1:iphone

    private boolean selected;

    public UserInfo() {
        id          = "";
        userId      = 0;
        username    = "";
        phone       = "";
        photoUrl    = "";
        rates       = new ArrayList<>();
        friends     = new ArrayList<>();
        deviceType  = Constants.DEVICE_ANDROID;

        selected    = false;
    }

    public UserInfo(UserInfo other) {
        id          = other.id;
        userId      = other.userId;
        username    = other.username;
        phone       = other.phone;
        photoUrl    = other.phone;
        rates       = other.rates;
        friends     = other.friends;
        deviceType  = other.deviceType;

        selected    = other.selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<Integer> getRates() {
        return rates;
    }

    public void setRates(List<Integer> rates) {
        for (int i = 0; i < rates.size(); i++) {
            this.rates.add(rates.get(i));
        }
    }

    public void setRates(ArrayList<Integer> rates) {
        this.rates = rates;
    }

    public int getRating() {
        if (rates.isEmpty())
            return Constants.LEVEL_NONE;

        int sum = 0;
        for (int i = 0; i < rates.size(); i++)
            sum += rates.get(i).intValue();
        return sum / rates.size();
    }

    public void setRating(int rating) {
        this.rates.clear();
        addRating(rating);
    }

    public void addRating(int rating) {
        if (rating == Constants.LEVEL_NONE)
            return;
        rates.add(Integer.valueOf(rating));
    }

    public boolean isEqual(UserInfo userInfo) {
        return id.equals(userInfo.getId());
    }

    public boolean isEqual(String id) {
        return this.id.equals(id);
    }

    public boolean isEqual(int userId) {
        return this.userId == userId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelect() {
        this.selected = !selected;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public ArrayList<UserInfo> getFriends() {
        return friends;
    }

    public ArrayList<String> getFriendIds() {
        if (friends == null)
            return null;

        ArrayList<String> friendIds = new ArrayList<>();
        for (UserInfo userInfo : friends) {
            friendIds.add(userInfo.getId());
        }

        return friendIds;
    }

    public void setFriends(ArrayList<UserInfo> friends) {
        this.friends = friends;
    }

    public void addFriend(UserInfo userInfo) {
        this.friends.add(userInfo);
    }

    public void addFriend(String userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        this.friends.add(userInfo);
    }

    public void constructFriends() {
        for (int i = 0; i < friends.size(); i++) {
            UserInfo userInfo = friends.get(i);
            friends.set(i, FiaApplication.getUserFromUserId(userInfo.getId()));
        }
    }

    public void removeFriend(UserInfo userInfo) {
        this.friends.remove(userInfo);
    }
}
