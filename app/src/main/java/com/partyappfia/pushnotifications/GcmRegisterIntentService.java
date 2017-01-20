package com.partyappfia.pushnotifications;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.io.IOException;
import java.util.ArrayList;

public class GcmRegisterIntentService extends IntentService {

	private static final String TAG      	= "GcmRegisterIntentService";
	private static final String SENDER_ID 	= "635836970117";//"595564954155";//
	private static final String TOPICS 		= "/topics/promote";

	public GcmRegisterIntentService() {
		super("GcmRegisterIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		try {
			InstanceID instanceId = InstanceID.getInstance(this);
			String token = instanceId.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
			//GcmPubSub.getInstance(this).subscribe(token, TOPICS, null);
			subscribeToPushNotifications(token);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("GCM - ", "registering failed");
		}
	}

	/**
	 * Subscribe to Push Notifications
	 *
	 * @param registrationID registration ID
	 */
	private void subscribeToPushNotifications(String registrationID) {
		//Create push token with  Registration Id for Android
		//
		Log.d(TAG, "subscribing...");

		QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
		subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
		//
		String deviceId;
		final TelephonyManager mTelephony = (TelephonyManager) getSystemService(
				Context.TELEPHONY_SERVICE);
		if (mTelephony.getDeviceId() != null) {
			deviceId = mTelephony.getDeviceId(); //*** use for mobiles
		} else {
			deviceId = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.ANDROID_ID); //*** use for tablets
		}
		subscription.setDeviceUdid(deviceId);
		//
		subscription.setRegistrationID(registrationID);
		//
		QBPushNotifications.createSubscription(subscription, new QBEntityCallback<ArrayList<QBSubscription>>() {

			@Override
			public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
				Log.d(TAG, "subscribed");
			}

			@Override
			public void onError(QBResponseException error) {
				Log.e(TAG, error.getLocalizedMessage());
			}
		});
	}
}
