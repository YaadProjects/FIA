package com.partyappfia.config;

/**
 * Created by Great Summit on 7/15/2016.
 */
public class Constants {
    // Main
    public static final String PREF_AUTOLOGIN                   = "pref_autologin";
    public static final String PREF_USERNAME                    = "pref_username";
    public static final String PREF_PASSWORD                    = "pref_password";
    public static final String PREF_FROM_PUSH                   = "pref_from_push";

    // Rating Level
    public static final int LEVEL_NONE                          = 0;
    public static final int LEVEL_LOW                           = 1;
    public static final int LEVEL_MIDDLE                        = 2;
    public static final int LEVEL_HIGH                          = 3;

    public static final int REQUEST_CAMERA                      = 100;
    public static final int REQUEST_GALLERY                     = 101;
    public static final int REQUEST_CROP                        = 102;

    public static final int NOTIFICATION_TYPE_MESSAGE           = 0;
    public static final int NOTIFICATION_TYPE_PARTY_INVITED     = 1;
    public static final int NOTIFICATION_TYPE_PARTY_ACCEPTED    = 2;
    public static final int NOTIFICATION_TYPE_PARTY_REJECTED    = 3;
    public static final int NOTIFICATION_TYPE_PARTY_CANCELED    = 4;
    public static final int NOTIFICATION_TYPE_PROCESSED         = 16;

    public static final int PARTY_STATE_CREATED                 = -1;
    public static final int PARTY_STATE_INVITED                 = 0;
    public static final int PARTY_STATE_ACCEPTED                = 1;
    public static final int PARTY_STATE_REJECTED                = 2;

    public static final String SPLIT_TAG                        = ",";

    public static final Boolean DEBUG                           = Boolean.TRUE;

    public static final int DEVICE_ANDROID                      = 0;
    public static final int DEVICE_IPHONE                       = 1;

    public static final int PARTY_DETAIL_TYPE_DETAIL            = 0;
    public static final int PARTY_DETAIL_TYPE_CREATE            = 1;
    public static final int PARTY_DETAIL_TYPE_EDIT              = 2;

    public static final String DATE_FORMAT_FOR_DISPLAY_PARTY    = "dd/MM/yyyy HH:mm";

    public static final String MESSAGE_REMOVED_MARK             = "<GSA-REMOVED>";
    public static final String MESSAGE_UPDATED_MARK             = "<GSA-UPDATED>";
}
