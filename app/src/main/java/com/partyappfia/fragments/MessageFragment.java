package com.partyappfia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.activities.ChatActivity;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.core.ChatService;
import com.partyappfia.dialogs.SingleSelectUserDialog;
import com.partyappfia.model.MessageItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.adapters.MessageListAdapter;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.TimeHelper;
import com.partyappfia.utils.Toaster;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Great Summit on 1/14/2016.
 */
public class MessageFragment extends BaseFragment {

    private static final int REQUEST_CHAT = 700;

    private ListView mListView;
    private MessageListAdapter mAdapter;
    private TextView tvEmptyMessage;

    private FloatingActionButton fabAddChatRoom;

    public QBDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new MessageListAdapter(this, new ArrayList<MessageItem>());
        mListView = (ListView)view.findViewById(R.id.lst_messages);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView)view.findViewById(R.id.tv_empty_message);

        fabAddChatRoom = (FloatingActionButton) view.findViewById(R.id.fab_add_chatroom);

        fabAddChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SingleSelectUserDialog(getHomeActivity(), new SingleSelectUserDialog.onOKButtonListener() {
                    @Override
                    public void onOKButton(UserInfo selectedUserInfo) {
                        if (selectedUserInfo == null)
                            return;

                        if (ChatService.getInstance().getCurrentUser() == null)
                            return;

                        ChatActivity.setPartnerUser(selectedUserInfo);
                        startActivityForResult(new Intent(MessageFragment.this.getContext(), ChatActivity.class), REQUEST_CHAT);
                        getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
                    }
                }).show();
            }
        });

        initialize();
    }

    private void initialize() {
        mAdapter.clear();

        if (ChatService.getInstance().getCurrentUser() == null)
            return;

        BusyHelper.show(getHomeActivity(), "Loading...");

        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();

        QBChatService.getChatDialogs(QBDialogType.GROUP, requestGetBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {
                BusyHelper.hide();

                if (dialogs == null) {
                    refreshControls();
                    return;
                }

                for (int i = 0; i < dialogs.size(); i++) {
                    QBDialog dialog = dialogs.get(i);
                    Log.d("Log", dialog.getLastMessageDateSent() + dialog.toString());
                    Log.d("Log", ChatService.getInstance().getCurrentUser().toString());
                    Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);
                    Date date = new Date(dialog.getLastMessageDateSent()*1000);
                    mAdapter.add(new MessageItem(FiaApplication.getUserFromUserId(opponentID), dialog.getLastMessage(), TimeHelper.getTimeStamp(date)));
                }

                refreshControls();
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
                BusyHelper.hide();
                Toaster.longToast(R.string.connection_failed);
            }
        });
    }

    private void refreshControls() {
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onSelectedMessage(MessageItem entity) {
        ChatActivity.setPartnerUser(entity.getUser());
        startActivityForResult(new Intent(this.getContext(), ChatActivity.class), REQUEST_CHAT);
        getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHAT) {
            initialize();
        }
    }
}
