package com.partyappfia.model;

/**
 * Created by Great Summit on 7/15/2016.
 */
public class ChatItem {
    private UserInfo    user;
    private String      dialogId;
    private String      messageId;
    private String      message;
    private String      uploadTime;
    private boolean     removed;

    public ChatItem() {
        dialogId    = "";
        messageId   = "";
        user        = null;
        message     = "";
        uploadTime  = "";
        removed     = false;
    }

    public ChatItem(String dialogId, String messageId, UserInfo user, String message, String uploadTime) {
        this.dialogId   = dialogId;
        this.messageId  = messageId;
        this.user       = user;
        this.message    = message;
        this.uploadTime = uploadTime;
        this.removed    = false;
    }

    public ChatItem(String dialogId, String messageId, UserInfo user, String message, String uploadTime, boolean removed) {
        this.dialogId   = dialogId;
        this.messageId  = messageId;
        this.user       = user;
        this.message    = message;
        this.uploadTime = uploadTime;
        this.removed    = removed;
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

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean bRemoved) {
        this.removed = bRemoved;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
