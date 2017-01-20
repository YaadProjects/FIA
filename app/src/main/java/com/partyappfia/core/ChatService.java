package com.partyappfia.core;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by igorkhomenko on 4/28/15.
 */
public class ChatService {

    private static final String TAG = ChatService.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 10;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatService instance;

    public static synchronized ChatService getInstance() {
        if(instance == null) {
            QBChatService.setDebugEnabled(true);
            instance = new ChatService();
        }
        return instance;
    }

    private QBChatService chatService;

    private ChatService() {
        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(chatConnectionListener);
        chatService.setReconnectionAllowed(true);
        chatService.setUseStreamManagement(true);
    }

    public void addConnectionListener(ConnectionListener listener){
        chatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener){
        chatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback callback){

        // Create REST API session
        //
        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                user.setId(session.getUserId());

                // login to Chat
                //
                loginToChat(user, new QBEntityCallback<Void>() {

                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        callback.onSuccess(result,bundle);
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        callback.onError(errors);
                    }
                });
            }

            @Override
            public void onError(QBResponseException errors) {
                callback.onError(errors);
            }
        });
    }

    public interface onSuccessListener {
        public void onSuccess(boolean bSuccess);
    }
    public void logout(final onSuccessListener listener){
        chatService.logout(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                if (listener != null)
                    listener.onSuccess(true);
            }

            @Override
            public void onError(QBResponseException list) {
                if (listener != null)
                    listener.onSuccess(false);
            }
        });
    }

    public void loginToChat(final QBUser user, final QBEntityCallback callback){

        chatService.login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                callback.onSuccess(result, b);
            }

            @Override
            public void onError(QBResponseException errors) {
                callback.onError(errors);
            }
        });
    }

    public void getDialogs(final QBEntityCallback callback){
        // get dialogs
        //
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setLimit(DIALOG_ITEMS_PER_PAGE);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {

                callback.onSuccess(dialogs, null);
            }

            @Override
            public void onError(QBResponseException errors) {
                callback.onError(errors);
            }
        });
    }


    private Map<Integer, QBUser> dialogsUsers = new HashMap<>();

    public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }

    public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    public Integer getOpponentIDForPrivateDialog(QBDialog dialog){
        Integer opponentID = -1;
        for(Integer userID : dialog.getOccupants()){
            if(!userID.equals(getCurrentUser().getId())){
                opponentID = userID;
                break;
            }
        }
        return opponentID;
    }


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
            if(seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
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


}
