package com.partyappfia.model;

/**
 * Created by Great Summit on 7/15/2016.
 */
public class MessageItem {
    private UserInfo    user;
    private String      message;
    private String      uploadTime;

    public MessageItem() {
        user        = null;
        message     = "";
        uploadTime  = "";
    }

    public MessageItem(UserInfo user, String message, String uploadTime) {
        this.user       = user;
        this.message    = message;
        this.uploadTime = uploadTime;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
