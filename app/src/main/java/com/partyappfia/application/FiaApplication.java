package com.partyappfia.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.partyappfia.R;
import com.partyappfia.activities.HomeActivity;
import com.partyappfia.activities.LoginActivity;
import com.partyappfia.config.Constants;
import com.partyappfia.core.ChatService;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.model.PartyItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.PrefManager;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Great Summit on 01/06/2016.
 */
public class FiaApplication extends Application {

    static final String APP_ID          = "44542";
    static final String AUTH_KEY        = "qx4jrDTcw9JRM3f";
    static final String AUTH_SECRET     = "CUq8RasnfUT-YsK";
    static final String ACCOUNT_KEY     = "6nPgWi9LwzDRixiGxqtr";

    private static FiaApplication       theApp;
    private static UserInfo             g_myUserInfo;
    private static UserInfo             g_SelectedUserInfo;
    private static ArrayList<UserInfo>  g_arrUsers;
    private static ArrayList<PartyItem> g_arrMyParties;
    private static ArrayList<PartyItem> g_arrInvitedParties;

    public static FiaApplication getApp() {
        return theApp;
    }

    public static UserInfo getMyUserInfo() {
        return g_myUserInfo;
    }

    public static void setMyUserInfo(UserInfo userInfo) {
        g_myUserInfo = userInfo;
    }

    public static UserInfo getSelectedUserInfo() {
        return g_SelectedUserInfo;
    }

    public static void setSelectedUserInfo(UserInfo selectedUserInfo) {
        FiaApplication.g_SelectedUserInfo = selectedUserInfo;
    }

    public static ArrayList<UserInfo> getUsers() {
        return g_arrUsers;
    }

    public static UserInfo getUserFromUserId(String userId) {
        if (g_arrUsers == null)
            return null;

        for (UserInfo userInfo : g_arrUsers) {
            if (userInfo.isEqual(userId))
                return userInfo;
        }

        if (g_myUserInfo != null && g_myUserInfo.isEqual(userId))
            return g_myUserInfo;

        return null;
    }

    public static UserInfo getUserFromUserId(int userIdOfUsers) {
        if (g_arrUsers == null)
            return null;

        for (UserInfo userInfo : g_arrUsers) {
            if (userInfo.isEqual(userIdOfUsers))
                return userInfo;
        }

        if (g_myUserInfo != null && g_myUserInfo.isEqual(userIdOfUsers))
            return g_myUserInfo;

        return null;
    }

    public static PartyItem getPartyFromPartyId(String partyId) {
        if (g_arrMyParties == null)
            return null;

        for (PartyItem partyItem : g_arrMyParties) {
            if (partyItem.isEqual(partyId))
                return partyItem;
        }

        if (g_arrInvitedParties == null)
            return null;

        for (PartyItem partyItem : g_arrInvitedParties) {
            if (partyItem.isEqual(partyId))
                return partyItem;
        }

        return null;
    }

    public static ArrayList<PartyItem> getMyParties() {
        return g_arrMyParties;
    }

    public static ArrayList<PartyItem> getInvitiedParties() {
        return g_arrInvitedParties;
    }

    public static ArrayList<PartyItem> getAcceptedParties() {
        ArrayList<PartyItem> arrAcceptedParties = new ArrayList<>();
        for (PartyItem partyItem : g_arrInvitedParties) {
            if (partyItem.getMyState() == Constants.PARTY_STATE_ACCEPTED)
                arrAcceptedParties.add(partyItem);
        }

        return arrAcceptedParties;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        theApp              = this;
        g_myUserInfo        = new UserInfo();
        g_arrUsers          = new ArrayList<UserInfo>();
        g_arrMyParties      = new ArrayList<PartyItem>();
        g_arrInvitedParties = new ArrayList<PartyItem>();

        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }

    public interface onUploadedDoneListener {
        public void onUploadedDone(String url);
    }
    public static void uploadPhotoUrl(String localUrl, final boolean bUpadteMyProfile, final onUploadedDoneListener listener) {
        File file = new File(localUrl);
        QBContent.uploadFileTask(file, true, "avatar", new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                final String photoUrl = qbFile.getPublicUrl();

                if (!bUpadteMyProfile) {
                    if (listener != null)
                        listener.onUploadedDone(photoUrl);
                    return;
                }

                QBCustomObject userInfoObject = new QBCustomObject();
                userInfoObject.setClassName("UserInfo");
                userInfoObject.setCustomObjectId(g_myUserInfo.getId());
                userInfoObject.put("photoUrl", photoUrl == null ? "" : photoUrl);

                QBCustomObjects.updateObject(userInfoObject, null, new QBEntityCallback<QBCustomObject>() {
                    @Override
                    public void onSuccess(QBCustomObject object, Bundle params) {
                        Log.d("Upload avatar", "Upload avatar success");
                        g_myUserInfo.setPhotoUrl(photoUrl);
                        if (listener != null)
                            listener.onUploadedDone(photoUrl);
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        if (listener != null)
                            listener.onUploadedDone(null);
                        Log.e("Upload avatar - ", errors.getMessage());
                        Toast.makeText(theApp, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(QBResponseException error) {
                if (listener != null)
                    listener.onUploadedDone(null);
                Log.e("Upload avatar - ", error.getMessage());
                Toast.makeText(theApp, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }

        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        });
    }

    public void signInWithReadingData(final Activity activity, final String username, final String password) {

        final QBUser user = new QBUser(username, password);
        if (activity instanceof LoginActivity)
            BusyHelper.show(activity, "Signing in...");

        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {
                QBUsers.signIn(user, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(final QBUser user, Bundle params) {
                        loginWithChat(activity, username, password);

                        PrefManager.savePrefString(Constants.PREF_USERNAME, username);
                        PrefManager.savePrefString(Constants.PREF_PASSWORD, password);

                        getMyUserInfo().setUserId(user.getId());
                        readDataFromServer(activity);
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        BusyHelper.hide();
                        Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                        gotoLoginActivity(activity);
                    }
                });
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                if (errors.getHttpStatusCode() == 401) { // QBResponseStatusCodeUnAuthorized
                    Toast.makeText(activity, "Username and password is not valid. Please try again", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
                gotoLoginActivity(activity);
            }
        });
    }

    private void loginWithChat(final Activity activity, final String username, final String password) {
        final QBUser user = new QBUser(username, password);

        ChatService.getInstance().login(user, new QBEntityCallback<Void>() {

            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.d("Log", "Login success with chat");
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
            }
        });
    }

    public void readDataFromServer(final Activity activity) {
        readUsers(new onSuccessListener() {
            @Override
            public void onSuccess(boolean bSuccess) {
                if (!bSuccess) {
                    BusyHelper.hide();
                    Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    gotoLoginActivity(activity);
                    return;
                }

                if (g_myUserInfo.getDeviceType() == Constants.DEVICE_ANDROID) {
                    readParties(new onSuccessListener() {
                        @Override
                        public void onSuccess(boolean bSuccess) {
                            BusyHelper.hide();
                            if (!bSuccess) {
                                Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                                return;
                            }
                            gotoHomeActivity(activity);
                        }
                    });
                    return;
                }

                QBCustomObject userInfoObject = new QBCustomObject();
                userInfoObject.setClassName("UserInfo");
                userInfoObject.setCustomObjectId(g_myUserInfo.getId());
                userInfoObject.put("deviceType", Constants.DEVICE_ANDROID);

                QBCustomObjects.updateObject(userInfoObject, null, new QBEntityCallback<QBCustomObject>() {
                    @Override
                    public void onSuccess(QBCustomObject object, Bundle params) {
                        Log.e("Update Device Type - ", "Success");
                        readParties(new onSuccessListener() {
                            @Override
                            public void onSuccess(boolean bSuccess) {
                                BusyHelper.hide();

                                if (!bSuccess) {
                                    Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                gotoHomeActivity(activity);
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        BusyHelper.hide();
                        Log.e("Update Device Type - ", errors.getMessage());
                        Toast.makeText(activity, R.string.connection_failed, Toast.LENGTH_LONG).show();
                        gotoLoginActivity(activity);
                    }
                });
            }
        });
    }

    public interface onSuccessListener {
        public void onSuccess(boolean bSuccess);
    }
    public static void readUsers(final onSuccessListener listener) {
        QBCustomObjects.getObjects("UserInfo", new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {

                if (customObjects == null) {
                    Log.d("Reading UserInfo - ", "UserInfo error");
                    if (listener != null)
                        listener.onSuccess(false);
                    return;
                }

                g_arrUsers.clear();
                for (QBCustomObject customObject : customObjects) {
                    UserInfo userInfo = getUserInfoFromCustomObject(customObject);
                    if (userInfo.getUsername().equalsIgnoreCase(PrefManager.readPrefString(Constants.PREF_USERNAME))) {
                        g_myUserInfo = userInfo;
                    } else {
                        g_arrUsers.add(userInfo);
                    }
                }
                g_myUserInfo.constructFriends();

                if (listener != null)
                    listener.onSuccess(true);
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.d("Reading UserInfo - ", errors.getMessage());
                if (listener != null)
                    listener.onSuccess(false);
            }
        });
    }

    public static void readParties(final onSuccessListener listener) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
//        requestBuilder.eq("ownerUserId",        g_myUserInfo.getId());
//        requestBuilder.in("invitiedUserIds",    g_myUserInfo.getId());
        requestBuilder.sortDesc("_id");

        QBCustomObjects.getObjects("Party", requestBuilder, new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {

                if (customObjects == null) {
                    Log.d("Reading PartyInfo - ", "PartyInfo error");
                    if (listener != null)
                        listener.onSuccess(false);
                    return;
                }

                g_arrMyParties.clear();
                g_arrInvitedParties.clear();

                for (QBCustomObject customObject : customObjects) {
                    PartyItem partyItem = new PartyItem();
                    partyItem.setId(customObject.getCustomObjectId());
                    if (customObject.get("title") != null && !customObject.get("title").equals(null))
                        partyItem.setTitle(customObject.getString("title"));
                    if (customObject.get("comment") != null && !customObject.get("comment").equals(null))
                        partyItem.setComment(customObject.getString("comment"));
                    if (customObject.get("address") != null && !customObject.get("address").equals(null))
                        partyItem.setAddress(customObject.getString("address"));
                    if (customObject.get("latLng") != null && !customObject.get("latLng").equals(null)) {
                        String[] latLngString = customObject.getString("latLng").split(Constants.SPLIT_TAG);
                        partyItem.setLatLng(new LatLng(Double.valueOf(latLngString[0]), Double.valueOf(latLngString[1])));
                    }
                    if (customObject.get("expireTime") != null && !customObject.get("expireTime").equals(null))
                        partyItem.setExpireTime(customObject.getString("expireTime"));
                    if (customObject.get("ownerUserId") != null && !customObject.get("ownerUserId").equals(null)) {
                        partyItem.setOwnerUser(getUserFromUserId(customObject.getString("ownerUserId")));
                    } else {
                        partyItem.setOwnerUser(getUserFromUserId(customObject.getUserId()));
                    }
                    List<Object> lstInvitedUserIds      = customObject.getArray("invitedUserIds");
                    List<Object> lstInvitedStates       = customObject.getArray("invitedStates");
                    List<Object> lstInvitedCheckTimes   = customObject.getArray("invitedCheckTimes");
                    if (lstInvitedUserIds != null) {
                        for (int i = 0; i < lstInvitedUserIds.size(); i++) {
                            InvitedItem invitedItem = new InvitedItem();
                            invitedItem.setUser(getUserFromUserId((String) lstInvitedUserIds.get(i)));
                            String stateString = (String)lstInvitedStates.get(i);
                            invitedItem.setState(Integer.valueOf(stateString));
                            invitedItem.setCheckTime((String) lstInvitedCheckTimes.get(i));
                            partyItem.getInvitedItems().add(invitedItem);
                        }
                    }

                    if (partyItem.getOwnerUser().isEqual(g_myUserInfo))
                        g_arrMyParties.add(partyItem);
                    else
                        g_arrInvitedParties.add(partyItem);
                }

                if (listener != null)
                    listener.onSuccess(true);
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.d("Reading PartyInfo - ", errors.getMessage());
                if (listener != null)
                    listener.onSuccess(false);
            }
        });
    }

    private static UserInfo getUserInfoFromCustomObject(QBCustomObject customObject) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(customObject.getCustomObjectId());
        userInfo.setUserId(customObject.getUserId());
        if (customObject.get("username") != null && !customObject.get("username").equals(null))
            userInfo.setUsername(customObject.getString("username"));
        if (customObject.get("phone") != null && !customObject.get("phone").equals(null))
            userInfo.setPhone(customObject.getString("phone"));
        if (customObject.get("photoUrl") != null && !customObject.get("photoUrl").equals(null))
            userInfo.setPhotoUrl(customObject.getString("photoUrl"));
        List<Object> friends = customObject.getArray("friends");
        if (friends != null) {
            for (Object obj : friends) {
                userInfo.addFriend(obj.toString());
            }
        }
        List<Object> rateObjects = customObject.getArray("rates");
        if (rateObjects != null) {
            for (Object obj : rateObjects) {
                userInfo.addRating(Integer.parseInt(obj.toString()));
            }
        }
        if (customObject.get("deviceType") != null)
            userInfo.setDeviceType(customObject.getInteger("deviceType"));

        return userInfo;
    }

    private void gotoHomeActivity(Activity activity) {
        PrefManager.savePrefBoolean(Constants.PREF_AUTOLOGIN, true);

        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.move_in_left, R.anim.move_out_left);
        activity.finish();
    }

    private void gotoLoginActivity(Activity activity) {
        if (activity instanceof LoginActivity)
            return;

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.move_in_left, R.anim.move_out_left);
        activity.finish();
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
