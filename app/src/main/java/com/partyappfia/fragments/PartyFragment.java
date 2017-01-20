package com.partyappfia.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.activities.PartyDetailActivity;
import com.partyappfia.adapters.PartyListAdapter;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.model.PartyItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.pushnotifications.QBPushHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.TimeHelper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;

/**
 * Created by Great Summit on 1/14/2016.
 */
public class PartyFragment extends BaseFragment {

    public static final int REQUEST_PARTY = 201;

    private TextView                tvMyParties;
    private TextView                tvInvitedParties;

    private FloatingActionButton    fabAddParty;

    private ListView                mListView;
    private PartyListAdapter        mAdapter;
    private TextView                tvEmptyMessage;

    private static boolean g_bMyPartyPage = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMyParties         = (TextView) view.findViewById(R.id.tv_my_parties);
        tvInvitedParties    = (TextView) view.findViewById(R.id.tv_invited_parties);
        fabAddParty         = (FloatingActionButton) view.findViewById(R.id.fab_add_party);

        mAdapter = new PartyListAdapter(this, new ArrayList<PartyItem>());
        mListView = (ListView) view.findViewById(R.id.lst_parties);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView) view.findViewById(R.id.tv_empty_message);

        fabAddParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PartyDetailActivity.setPartyItem(null);
                Intent intent = new Intent(getHomeActivity(), PartyDetailActivity.class);
                intent.putExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_TYPE, Constants.PARTY_DETAIL_TYPE_CREATE);
                startActivityForResult(intent, REQUEST_PARTY);
                getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
            }
        });

        tvMyParties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g_bMyPartyPage = true;
                initialize();
            }
        });

        tvInvitedParties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g_bMyPartyPage = false;
                initialize();
            }
        });

        initialize();
    }

    private void initialize() {

        mAdapter.clear();
        ArrayList<PartyItem> arrayList = g_bMyPartyPage ? FiaApplication.getMyParties() : FiaApplication.getAcceptedParties();
        for (PartyItem partyItem : arrayList) {
            mAdapter.add(partyItem);
        }

        refreshControls();
    }

    private void refreshControls() {
        fabAddParty.setVisibility(g_bMyPartyPage ? View.VISIBLE : View.GONE);
        tvMyParties.setTextColor(g_bMyPartyPage ? getResources().getColor(R.color.colorOrange) : Color.WHITE);
        tvInvitedParties.setTextColor(g_bMyPartyPage ? Color.WHITE : getResources().getColor(R.color.colorOrange));

        mAdapter.notifyDataSetChanged();
        mAdapter.setLockSwipe(!g_bMyPartyPage);
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onSelectedParty(PartyItem entity) {
        PartyDetailActivity.setPartyItem(entity);
        Intent intent = new Intent(getHomeActivity(), PartyDetailActivity.class);
        intent.putExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_TYPE, Constants.PARTY_DETAIL_TYPE_DETAIL);
        startActivityForResult(intent, REQUEST_PARTY);
        getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    public void onDeleteItemClicked(final PartyItem partyItem) {
        if (!g_bMyPartyPage)
            return;

        BusyHelper.show(getHomeActivity(), "Deleting...");
        QBCustomObject customObject = new QBCustomObject("Party", partyItem.getId());
        QBCustomObjects.deleteObject(customObject, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                BusyHelper.hide();

                ArrayList<QBCustomObject> objects = new ArrayList<QBCustomObject>();
                for (InvitedItem invitedItem : partyItem.getInvitedItems()) {
                    UserInfo userInfo = invitedItem.getUser();

                    QBCustomObject object = new QBCustomObject();
                    object.setClassName("Notification");

                    String message = String.format("Sorry! %s cancel the party - \"%s\".", partyItem.getOwnerUser().getUsername(), partyItem.getTitle());
                    object.putInteger("notificationType",   Constants.NOTIFICATION_TYPE_PARTY_CANCELED);
                    object.putString("message",             message);
                    object.putString("senderUserId",        partyItem.getOwnerUser().getId());
                    object.putString("receiverUserId",      userInfo.getId());
                    object.putString("time",                TimeHelper.getTimeStamp());
                    object.putString("partyId",             partyItem.getId());

                    objects.add(object);
                    QBPushHelper.sendPushNotification(userInfo, message);
                }

                QBCustomObjects.createObjects(objects, null);

                FiaApplication.getMyParties().remove(partyItem);
                mAdapter.remove(partyItem);
                refreshControls();
            }

            @Override
            public void onError(QBResponseException e) {
                BusyHelper.hide();
                Log.d("Deleting Party - ", e.getMessage());
                Toast.makeText(getHomeActivity(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PARTY && data != null) {
            if (g_bMyPartyPage && data.hasExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_RESUT)) {
                String partyId = data.getStringExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_RESUT);
                PartyItem newPartyItem = null;
                for (PartyItem partyItem : FiaApplication.getMyParties()) {
                    if (partyItem.isEqual(partyId)) {
                        newPartyItem = partyItem;
                        break;
                    }
                }

                if (newPartyItem != null) {
                    boolean bExist = false;
                    ArrayList<PartyItem> arrPartyItems = mAdapter.getList();
                    if (arrPartyItems != null) {
                        for (PartyItem partyItem : arrPartyItems) {
                            if (partyItem.isEqual(newPartyItem)) {
                                bExist = true;
                                break;
                            }
                        }
                    }

                    if (!bExist) {
                        mAdapter.insert(newPartyItem, 0);
                    }
                }
            }

            refreshControls();
        }
    }
}
