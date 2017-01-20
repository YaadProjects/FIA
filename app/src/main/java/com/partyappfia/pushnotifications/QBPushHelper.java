package com.partyappfia.pushnotifications;

import android.os.Bundle;
import android.util.Log;

import com.partyappfia.config.Constants;
import com.partyappfia.model.UserInfo;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;

/**
 * Created by Great Summit on 7/30/2016.
 */
public class QBPushHelper {

    public static void sendPushNotification(UserInfo userInfo, String message) {
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.add(userInfo.getUserId());

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(userInfo.getDeviceType() == Constants.DEVICE_ANDROID ? QBPushType.GCM: QBPushType.APNS);
        event.setMessage(message);

        QBPushNotifications.createEvent(event, new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle args) {
                // sent
                Log.d("GCM", "send push notification success!");
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.d("GCM", "send push notification failed!");
                errors.printStackTrace();
            }
        });
    }
}
