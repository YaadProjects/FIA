package com.partyappfia.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.partyappfia.R;
import com.partyappfia.adapters.ChatListAdapter;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.core.Chat;
import com.partyappfia.core.ChatService;
import com.partyappfia.core.GroupChatImpl;
import com.partyappfia.model.ChatItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.KeyboardHelper;
import com.partyappfia.utils.TimeHelper;
import com.partyappfia.utils.Toaster;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends BaseActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private ListView            mListView;
    private ChatListAdapter     mAdapter;
    private SwipyRefreshLayout  swipyRefreshLayout;

    private int                 skipCount = 0;
    private static final int    PAGE_COUNT = 20;

    private EditText            evInput;

    public QBDialog             qbDialog;
    private Chat                chat;

    private static UserInfo g_partnerUser = null;

    public static void setPartnerUser(UserInfo partnerUser) {
        ChatActivity.g_partnerUser = partnerUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAdapter = new ChatListAdapter(this, new ArrayList<ChatItem>());
        mListView = (ListView) findViewById(R.id.lst_messages);
        //mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setStackFromBottom(true);
        mListView.setAdapter(mAdapter);
        swipyRefreshLayout = (SwipyRefreshLayout)findViewById(R.id.swipy_refresh_layout);

        evInput = (EditText) findViewById(R.id.ev_input);

        findViewById(R.id.lv_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendMessage();
            }
        });

        swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                loadChatHistory(false);
            }
        });

        findViewById(R.id.lv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initialize();
    }

    private void initialize() {
        mAdapter.clear();

        if (ChatService.getInstance().getCurrentUser() == null)
            return;

        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();

        BusyHelper.show(ChatActivity.this, "Loading...");
        QBChatService.getChatDialogs(QBDialogType.GROUP, requestGetBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {
                if (dialogs == null) {
                    createChatDialog(g_partnerUser);
                    return;
                }

                for (int i = 0; i < dialogs.size(); i++) {
                    QBDialog dialog = dialogs.get(i);
                    Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);
                    if (g_partnerUser.getUserId() == opponentID) {
                        qbDialog = dialog;
                        joinGroupChat();
                        return;
                    }
                }

                createChatDialog(g_partnerUser);
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
                BusyHelper.hide();
                Toaster.longToast(R.string.connection_failed);
            }
        });
    }

    @Override
    public void onBackPressed() {
        KeyboardHelper.hideKeyboard(this);
        finish();
        overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    private void onClickSendMessage() {
        String message = evInput.getText().toString();
        if (message.isEmpty())
            return;

        if (ChatService.getInstance().getCurrentUser() == null)
            return;

        sendMessage(message, true);
        evInput.setText("");
    }

    public void createChatDialog(UserInfo opponent) {
        QBUser user = new QBUser();
        user.setId(opponent.getUserId());
        user.setLogin(opponent.getUsername());

        List<QBUser> userList = new ArrayList<QBUser>();
        userList.add(user);

        ChatService.getInstance().addDialogsUsers(userList);

        // Create new group dialog
        //
        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName(FiaApplication.getMyUserInfo().getUsername() + "_" + opponent.getUsername());
        dialogToCreate.setType(QBDialogType.GROUP);
        dialogToCreate.setOccupantsIds(getUserIds(opponent));

        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallback<QBDialog>() {
            @Override
            public void onSuccess(QBDialog dialog, Bundle args) {
                qbDialog = dialog;
                joinGroupChat();
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Toast.makeText(ChatActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                errors.printStackTrace();
            }
        });
    }

    public static ArrayList<Integer> getUserIds(UserInfo entity){
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(entity.getUserId());
        return ids;
    }

    private void joinGroupChat() {
        chat = new GroupChatImpl(this);
        ((GroupChatImpl) chat).joinGroupChat(qbDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                if (isSessionActive()) {
                    loadChatHistory(false);
                }

                ChatService.getInstance().addConnectionListener(chatConnectionListener);
            }

            @Override
            public void onError(QBResponseException e) {
                BusyHelper.hide();
                e.printStackTrace();
            }
        });
    }

    private void loadChatHistory(boolean bShowBusy) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(PAGE_COUNT);
        requestBuilder.sortDesc("date_sent");
        requestBuilder.setSkip(skipCount);

        if (bShowBusy)
            BusyHelper.show(ChatActivity.this, "Loading...");

        QBChatService.getDialogMessages(qbDialog, requestBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                BusyHelper.hide();
                swipyRefreshLayout.setRefreshing(false);

                for (int i = 0; i < messages.size(); i ++) {
                    QBChatMessage msg = messages.get(i);

                    if (msg.getBody().contains(Constants.MESSAGE_UPDATED_MARK))
                        continue;

                    Log.d("Log", msg.toString());
                    String strmsg = msg.getBody().replace("&quot;", "\"");
                    boolean bRemoved = strmsg.contains(Constants.MESSAGE_REMOVED_MARK);
                    if (msg.getSenderId() == g_partnerUser.getUserId())
                        mAdapter.insert(new ChatItem(msg.getDialogId(), msg.getId(), g_partnerUser, strmsg.replace(Constants.MESSAGE_REMOVED_MARK, ""), String.valueOf(msg.getDateSent()), bRemoved), 0);
                    else {
                        mAdapter.insert(new ChatItem(msg.getDialogId(), msg.getId(), FiaApplication.getMyUserInfo(), strmsg.replace(Constants.MESSAGE_REMOVED_MARK, ""), String.valueOf(msg.getDateSent()), bRemoved), 0);
                    }
                }

                skipCount += PAGE_COUNT;
            }

            @Override
            public void onError(QBResponseException errors) {
                swipyRefreshLayout.setRefreshing(false);
                BusyHelper.hide();
                errors.printStackTrace();
                Toaster.longToast(R.string.connection_failed);
            }
        });
    }

    public void addMessage(QBChatMessage message) {
        if (mAdapter == null || mListView == null)
            return;

        String strmsg = message.getBody().replace("&quot;", "\"");

        if (strmsg.contains(Constants.MESSAGE_UPDATED_MARK)) {
            if (message.getSenderId() == FiaApplication.getMyUserInfo().getUserId())
                return;

            String messageId = strmsg.replace(Constants.MESSAGE_UPDATED_MARK, "");
            for (ChatItem chatItem : mAdapter.getList()) {
                if (chatItem.getMessageId().equals(messageId)) {
                    chatItem.setRemoved(true);
                    mAdapter.notifyDataSetChanged();
                    break;
                }
            }
            return;
        }

        UserInfo userInfo = FiaApplication.getUserFromUserId(message.getSenderId());
        mAdapter.add(new ChatItem(message.getDialogId(), message.getId(), userInfo, strmsg, TimeHelper.getTimeStamp()));
        mListView.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    //////////////////////////////////////////////////////////////////
    //  Receive Message Event Processing
    public static QBChatMessage s_ChatMessageHandlerData = null;
    private final Handler HANDLER_CHATMESSAGE = new Handler() {
        // Create handleMessage function
        public void handleMessage(Message msg) {
            addMessage(s_ChatMessageHandlerData);
        }
    };

    // This function is called from thread
    public void onReceivedMessage(QBChatMessage message) {
        String strmsg = message.getBody().replace("&quot;", "\"");
        Log.e(TAG, "chat message=" + strmsg);
        s_ChatMessageHandlerData = message;
        HANDLER_CHATMESSAGE.sendEmptyMessage(0);
    }
    //////////////////////////////////////////////////////////////////

    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean authenticated) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());
        }

        @Override
        public void reconnectingIn(final int seconds) {
            Log.i(TAG, "reconnectingIn: " + seconds);
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };

    private void sendMessage(String message, boolean bSaveToHistory) {
        if (chat == null) {
            Toaster.shortToast(R.string.connection_failed);
            return;
        }

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(message);
        if (bSaveToHistory)
            chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        Log.d(TAG, String.format("send message=%s", message));
        try {
            if (chat != null)
                chat.sendMessage(chatMessage);
        } catch (SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
            Toaster.shortToast(R.string.connection_failed);
        } catch (XMPPException e) {
            e.printStackTrace();
            Toaster.shortToast(R.string.connection_failed);
        } catch (Exception e) {
            e.printStackTrace();
            Toaster.shortToast(R.string.connection_failed);
        }
    }

    public void onRightPaneLongClicked(final ChatItem item) {
        if (item.isRemoved())
            return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.delete_message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeMessage(item);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void removeMessage(final ChatItem item) {
        item.setRemoved(true);
        mAdapter.notifyDataSetChanged();

        QBMessageUpdateBuilder qbMessageUpdateBuilder = new QBMessageUpdateBuilder();
        qbMessageUpdateBuilder.updateText(item.getMessage() + Constants.MESSAGE_REMOVED_MARK);
        QBChatService.updateMessage(item.getMessageId(), item.getDialogId(), qbMessageUpdateBuilder, null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(Constants.MESSAGE_UPDATED_MARK + item.getMessageId(), false);
            }
        }, 50);
    }

    public void onUserIsTyping(Integer userId) {
        Toast.makeText(this, "Typing...", Toast.LENGTH_LONG).show();
    }

    public void onUserStopTyping(Integer userId) {
        Toast.makeText(this, "Stop Typing...", Toast.LENGTH_LONG).show();
    }
}
