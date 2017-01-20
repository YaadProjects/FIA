package com.partyappfia.core;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public interface Chat {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void updateLastMessage(QBDialog qbDialog, String lastMessage);

    void release() throws XMPPException;
}
