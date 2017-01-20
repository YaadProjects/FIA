package com.partyappfia.model;

import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;

/**
 * Created by Great Summit on 7/26/2016.
 */
public class NotificationItem {
    private String      id;
    private int         notificationType;
    private String      message;
    private String      uploadTime;
    private UserInfo    senderUser;
    private UserInfo    receiverUser;
    private PartyItem   partyItem;

    public NotificationItem() {
        id                  = "";
        notificationType    = Constants.NOTIFICATION_TYPE_MESSAGE;
        message             = "";
        uploadTime          = "";
        senderUser          = null;
        receiverUser        = null;
        partyItem           = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
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

    public UserInfo getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(UserInfo senderUser) {
        this.senderUser = senderUser;
    }

    public UserInfo getReceiverUser() {
        return receiverUser;
    }

    public void setReceiverUser(UserInfo receiverUser) {
        this.receiverUser = receiverUser;
    }

    public PartyItem getPartyItem() {
        return partyItem;
    }

    public void setPartyItem(PartyItem partyItem) {
        this.partyItem = partyItem;
    }
}
