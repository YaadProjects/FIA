package com.partyappfia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.activities.ProfileActivity;
import com.partyappfia.dialogs.MultiSelectUserDialog;
import com.partyappfia.dialogs.SelectPartyDialog;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.model.PartyItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.adapters.HomeUserListAdapter;
import com.partyappfia.pushnotifications.QBPushHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.KeyboardHelper;
import com.partyappfia.utils.TimeHelper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;

/**
 * Created by Great Summit on 1/14/2016.
 */
public class HomeFragment extends BaseFragment {

    public static final int REQUEST_PROFILE     = 120;

    private View                    lvSelectAll;
    private ImageView               ivSelectAll;
    private TextView                tvSelectAll;
    private View                    lvDelete;
    private View                    lvInvite;
    private EditText                evSearch;
    private View                    lvCancel;

    private ListView                mListView;
    private HomeUserListAdapter     mAdapter;
    private TextView                tvEmptyMessage;
    private FloatingActionButton    fabAddFriends;

    private boolean     bSelected = false;

    private static boolean  bFirstTime = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvSelectAll = view.findViewById(R.id.lv_select_all);
        ivSelectAll = (ImageView) view.findViewById(R.id.iv_select_all);
        tvSelectAll = (TextView) view.findViewById(R.id.tv_select_all);
        evSearch    = (EditText) view.findViewById(R.id.ev_search);
        lvCancel    = view.findViewById(R.id.lv_cancel);
        lvDelete    = view.findViewById(R.id.lv_delete);
        lvInvite    = view.findViewById(R.id.lv_invite);

        mAdapter = new HomeUserListAdapter(this, new ArrayList<UserInfo>());
        mListView = (ListView)view.findViewById(R.id.lst_users);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView)view.findViewById(R.id.tv_empty_message);
        fabAddFriends = (FloatingActionButton) view.findViewById(R.id.fab_add_friends);

        fabAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddUserButtonClicked();
            }
        });

        lvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSelected = !bSelected;
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    UserInfo item = mAdapter.getItem(i);
                    item.setSelected(bSelected);
                }
                mAdapter.notifyDataSetChanged();
                refreshControls();
            }
        });

        evSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = evSearch.getText().toString().trim();
                lvCancel.setVisibility(searchText.isEmpty() ? View.GONE : View.VISIBLE);
                search(searchText, false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evSearch.setText("");
            }
        });

        lvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UserInfo item : mAdapter.getList()) {
                    if (!item.isSelected())
                        continue;
                    FiaApplication.getMyUserInfo().removeFriend(item);
                    mAdapter.remove(item);
                }
                updateMyUserInfo("Delete friend...");
                refreshControls();
            }
        });

        lvInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectPartyDialog(getHomeActivity(), getSelectedUsers(), new SelectPartyDialog.onOKButtonListener() {
                    @Override
                    public void onOKButton(PartyItem partyItem, ArrayList<UserInfo> candidates) {
                        invitingUsers(partyItem, candidates);
                    }
                }).show();
            }
        });

        initialize();
    }

    private void initialize() {
        if (bFirstTime) {
            search("", true);
            return;
        }

        BusyHelper.show(getHomeActivity(), "Downloading...");
        FiaApplication.readUsers(new FiaApplication.onSuccessListener() {
            @Override
            public void onSuccess(boolean bSuccess) {
                BusyHelper.hide();
                if (!bSuccess) {
                    Toast.makeText(getHomeActivity(), R.string.connection_failed, Toast.LENGTH_LONG).show();
                    refreshControls();
                    return;
                }
                search("", true);
            }
        });
    }

    public void search(String searchText, boolean bCleanSelecting) {
        searchText = searchText.toLowerCase().trim();

        if (mAdapter != null)
            mAdapter.clear();

        for (UserInfo userInfo : FiaApplication.getMyUserInfo().getFriends()) {
            if (!searchText.isEmpty()) {
                if (!userInfo.getUsername().contains(searchText))
                    continue;
            }

            if (bCleanSelecting)
                userInfo.setSelected(false);
            mAdapter.add(userInfo);
        }

        refreshControls();
    }

    public void refreshControls() {
        int selectedCount = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            UserInfo item = mAdapter.getItem(i);
            if (item.isSelected()) {
                selectedCount++;
            }
        }

        bSelected = (selectedCount == mAdapter.getCount());
        tvSelectAll.setText(String.format("   %d/%d selected", selectedCount, mAdapter.getCount()));
        ivSelectAll.setImageResource(selectedCount > 0 && bSelected ? R.drawable.checkbox_selected : R.drawable.checkbox_white);
        lvDelete.setVisibility(selectedCount > 0 ? View.VISIBLE : View.GONE);
        lvInvite.setVisibility(selectedCount > 0 ? View.VISIBLE : View.GONE);

        refreshListView();
    }

    public void refreshListView() {
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private ArrayList<UserInfo> getSelectedUsers() {
        ArrayList<UserInfo> selectedUsers = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            UserInfo item = mAdapter.getItem(i);
            if (!item.isSelected())
                continue;
            selectedUsers.add(item);
        }

        return selectedUsers;
    }

    private void invitingUsers(final PartyItem partyItem, final ArrayList<UserInfo> candidates) {
        if (partyItem == null)
            return;

        if (candidates == null || candidates.isEmpty())
            return;

        QBCustomObject object = new QBCustomObject();
        object.setClassName("Party");
        object.setCustomObjectId(partyItem.getId());

        object.putArray("invitedUserIds",       partyItem.getInvitedUserIds());
        object.putArray("invitedStates",        partyItem.getInvitedStates());
        object.putArray("invitedCheckTimes",    partyItem.getInvitedCheckTimes());

        BusyHelper.show(getContext(), "Inviting users...");
        QBCustomObjects.updateObject(object, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(final QBCustomObject createdObject, Bundle params) {
                BusyHelper.hide();

                if (candidates != null && !candidates.isEmpty()) {
                    ArrayList<QBCustomObject> objects = new ArrayList<QBCustomObject>();

                    for (UserInfo userInfo : candidates) {
                        QBCustomObject object = new QBCustomObject();
                        object.setClassName("Notification");

                        String message = String.format("Welcome! %s invited you to the party - \"%s\". %s is waiting for your answer with accepting or rejecting.", partyItem.getOwnerUser().getUsername(), partyItem.getTitle(), partyItem.getOwnerUser().getUsername());
                        object.putInteger("notificationType", Constants.NOTIFICATION_TYPE_PARTY_INVITED);
                        object.putString("message", message);
                        object.putString("senderUserId", partyItem.getOwnerUser().getId());
                        object.putString("receiverUserId", userInfo.getId());
                        object.putString("time", TimeHelper.getTimeStamp());
                        object.putString("partyId", partyItem.getId());

                        objects.add(object);
                        QBPushHelper.sendPushNotification(userInfo, message);
                    }

                    QBCustomObjects.createObjects(objects, null);
                }
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Log.d("Updating party - ", errors.getMessage());
                Toast.makeText(getHomeActivity(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onAddUserButtonClicked() {
        new MultiSelectUserDialog(getHomeActivity(), false, new MultiSelectUserDialog.onOKButtonListener() {
            @Override
            public void onOKButton(ArrayList<UserInfo> selectedUsers) {
                if (selectedUsers == null)
                    return;

                int addingCount = 0;
                ArrayList<UserInfo> arrFriends = mAdapter.getList();
                for (UserInfo userInfo : selectedUsers) {
                    if (arrFriends != null) {
                        boolean bExist = false;
                        for (UserInfo friend : arrFriends) {
                            if (friend.isEqual(userInfo)) {
                                bExist = true;
                                break;
                            }
                        }
                        if (bExist)
                            continue;
                    }

                    addingCount++;
                    userInfo.setSelected(false);
                    mAdapter.add(userInfo);
                    FiaApplication.getMyUserInfo().addFriend(userInfo);
                }

                if (addingCount > 0) {
                    updateMyUserInfo(addingCount == 1 ? "Adding friend..." : "Adding friends...");
                }

                refreshControls();

            }
        }).show();
    }

    private void updateMyUserInfo(String busyMessage) {
        QBCustomObject userInfoObject = new QBCustomObject();
        userInfoObject.setClassName("UserInfo");
        userInfoObject.setCustomObjectId(FiaApplication.getMyUserInfo().getId());
        userInfoObject.putArray("friends", FiaApplication.getMyUserInfo().getFriendIds());

        //BusyHelper.show(getContext(), busyMessage);
        QBCustomObjects.updateObject(userInfoObject, null, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject object, Bundle params) {
                BusyHelper.hide();
                Log.d("Adding friends to MyProfile", "success");
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Log.e("Update MyProfile - ", errors.getMessage());
                Toast.makeText(HomeFragment.this.getContext(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        KeyboardHelper.hideKeyboard(this);
    }

    public void onSelectedUser(UserInfo item) {
        ProfileActivity.setUserInfo(item);
        startActivityForResult(new Intent(getContext(), ProfileActivity.class), REQUEST_PROFILE);
        getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROFILE) {
            refreshListView();
        }
    }

    public void onDeleteUserButtonClicked(UserInfo item) {
        FiaApplication.getMyUserInfo().removeFriend(item);
        mAdapter.remove(item);
        updateMyUserInfo("Delete friend...");
        refreshControls();
    }
}
