package com.partyappfia.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.partyappfia.R;
import com.partyappfia.adapters.NotificationListAdapter;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.model.NotificationItem;
import com.partyappfia.model.PartyItem;
import com.partyappfia.pushnotifications.QBPushHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.TimeHelper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;


public class NotificationActivity extends AppCompatActivity {

    private ListView                mListView;
    private NotificationListAdapter mAdapter;
    private SwipyRefreshLayout      swipyRefreshLayout;
    private TextView                tvEmptyMessage;

    private int                     skipCount   = 0;
    private static final int        PAGE_COUNT  = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAdapter = new NotificationListAdapter(this, new ArrayList<NotificationItem>());
        mListView = (ListView) findViewById(R.id.lst_notifications);
        swipyRefreshLayout = (SwipyRefreshLayout)findViewById(R.id.swipy_refresh_layout);
        mListView.setAdapter(mAdapter);

        swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                downloadFromServer(false);
            }
        });

        tvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

        findViewById(R.id.lv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initialize();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    private void initialize() {
        skipCount = 0;

        mAdapter.clear();

        BusyHelper.show(this, "Downloading...");
        FiaApplication.readUsers(new FiaApplication.onSuccessListener() {
            @Override
            public void onSuccess(boolean bSuccess) {
                if (!bSuccess) {
                    BusyHelper.hide();
                    Toast.makeText(NotificationActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    return;
                }

                FiaApplication.readParties(new FiaApplication.onSuccessListener() {
                    @Override
                    public void onSuccess(boolean bSuccess) {
                        if (!bSuccess) {
                            BusyHelper.hide();
                            Toast.makeText(NotificationActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                            return;
                        }

                        downloadFromServer(false);
                    }
                });
            }
        });
    }

    private void downloadFromServer(boolean bShowBusy) {

        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.eq("receiverUserId", FiaApplication.getMyUserInfo().getId());
        requestBuilder.sortDesc("_id");
        requestBuilder.setSkip(skipCount);
        requestBuilder.setLimit(PAGE_COUNT);

        if (bShowBusy)
            BusyHelper.show(this, "Downloading...");

        QBCustomObjects.getObjects("Notification", requestBuilder, new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
                swipyRefreshLayout.setRefreshing(false);

                if (customObjects == null) {
                    BusyHelper.hide();
                    Log.d("Reading Notifications - ", "Notification error");
                    Toast.makeText(NotificationActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    refreshControls();
                    return;
                }

                if (customObjects.isEmpty()) {
                    BusyHelper.hide();
                    refreshControls();
                    return;
                }

                for (QBCustomObject customObject : customObjects) {
                    NotificationItem notificationItem = new NotificationItem();
                    notificationItem.setId(customObject.getCustomObjectId());
                    if (customObject.get("notificationType") != null && !customObject.get("notificationType").equals(null))
                        notificationItem.setNotificationType(customObject.getInteger("notificationType"));
                    if (customObject.get("message") != null && !customObject.get("message").equals(null))
                        notificationItem.setMessage(customObject.getString("message"));
                    if (customObject.get("time") != null && !customObject.get("time").equals(null))
                        notificationItem.setUploadTime(customObject.getString("time"));
                    if (customObject.get("senderUserId") != null && !customObject.get("senderUserId").equals(null)) {
                        String sendUserId = customObject.getString("senderUserId");
                        notificationItem.setSenderUser(FiaApplication.getUserFromUserId(sendUserId));
                    } else {
                        notificationItem.setSenderUser(FiaApplication.getUserFromUserId(customObject.getUserId()));
                    }
                    if (customObject.get("receiverUserId") != null && !customObject.get("receiverUserId").equals(null)) {
                        String receiverUserId = customObject.getString("receiverUserId");
                        notificationItem.setReceiverUser(FiaApplication.getUserFromUserId(receiverUserId));
                    } else {

                    }
                    if (customObject.get("partyId") != null && !customObject.get("partyId").equals(null)) {
                        String partyId = customObject.getString("partyId");
                        notificationItem.setPartyItem(FiaApplication.getPartyFromPartyId(partyId));
                    }

                    if (notificationItem.getNotificationType() == Constants.NOTIFICATION_TYPE_PARTY_CANCELED) {
                        for (PartyItem partyItem : FiaApplication.getInvitiedParties()) {
                            if (partyItem.isEqual(notificationItem.getPartyItem())) {
                                FiaApplication.getInvitiedParties().remove(partyItem);
                                break;
                            }
                        }
                    }

                    mAdapter.add(notificationItem);
                }

                skipCount += PAGE_COUNT;
                refreshControls();
                BusyHelper.hide();
            }

            @Override
            public void onError(QBResponseException errors) {
                swipyRefreshLayout.setRefreshing(false);
                BusyHelper.hide();
                Log.d("Reading Notifications - ", errors.getMessage());
                Toast.makeText(NotificationActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshControls() {
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onSelectedNotification(final NotificationItem entity) {
        if (entity.getNotificationType() == Constants.NOTIFICATION_TYPE_PARTY_INVITED) {
            if (entity.getPartyItem() == null)
                return;

            new AlertDialog.Builder(NotificationActivity.this)
                    .setTitle(R.string.app_name)
                    .setMessage("Are you sure to accept this inviting?")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            processInviting(entity, true);
                        }
                    })
                    .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            processInviting(entity, false);
                        }
                    })
                    .show();
        }
    }

    private void processInviting(final NotificationItem entity, final boolean bAccepted) {
        if (entity.getPartyItem() == null)
            return;

        QBCustomObject object = new QBCustomObject();
        object.setClassName("Party");
        object.setCustomObjectId(entity.getPartyItem().getId());
        for (InvitedItem invitedItem : entity.getPartyItem().getInvitedItems()) {
            if (invitedItem.getUser().isEqual(FiaApplication.getMyUserInfo())) {
                invitedItem.setState(bAccepted ? Constants.PARTY_STATE_ACCEPTED : Constants.PARTY_STATE_REJECTED);
                break;
            }
        }
        object.putArray("invitedUserIds",       entity.getPartyItem().getInvitedUserIds());
        object.putArray("invitedStates",        entity.getPartyItem().getInvitedStates());
        object.putArray("invitedCheckTimes",    entity.getPartyItem().getInvitedCheckTimes());

        BusyHelper.show(this, bAccepted ? "Accepting..." : "Rejecting...");
        QBCustomObjects.updateObject(object, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(final QBCustomObject createdObject, Bundle params) {
                BusyHelper.hide();

                QBCustomObject object = new QBCustomObject();
                object.setClassName("Notification");
                object.setCustomObjectId(entity.getId());
                object.putInteger("notificationType", entity.getNotificationType() | Constants.NOTIFICATION_TYPE_PROCESSED);
                QBCustomObjects.updateObject(object, new QBEntityCallback<QBCustomObject>() {
                    @Override
                    public void onSuccess(QBCustomObject customObject, Bundle bundle) {

                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });


                object = new QBCustomObject();
                object.setClassName("Notification");

                String message = String.format("%s %s your inviting for the pary - \"%s\"", entity.getReceiverUser().getUsername(), bAccepted ? "accepted" : "rejected", entity.getPartyItem().getTitle());
                object.putInteger("notificationType",   bAccepted ? Constants.NOTIFICATION_TYPE_PARTY_ACCEPTED : Constants.NOTIFICATION_TYPE_PARTY_REJECTED);
                object.putString("message",             message);
                object.putString("senderUserId",        entity.getReceiverUser().getId());
                object.putString("receiverUserId",      entity.getSenderUser().getId());
                object.putString("time",                TimeHelper.getTimeStamp());
                object.putString("partyId",             entity.getPartyItem().getId());

                QBCustomObjects.createObject(object, null);

                QBPushHelper.sendPushNotification(entity.getSenderUser(), message);

                entity.setNotificationType(Constants.NOTIFICATION_TYPE_PROCESSED);

                refreshControls();

                if (!bAccepted) {
                    return;
                }

                PartyDetailActivity.setPartyItem(entity.getPartyItem());
                Intent intent = new Intent(NotificationActivity.this, PartyDetailActivity.class);
                intent.putExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_TYPE, Constants.PARTY_DETAIL_TYPE_DETAIL);
                startActivity(intent);
                overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Log.d("Updating party - ", errors.getMessage());
                Toast.makeText(NotificationActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }
}
