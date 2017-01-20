package com.partyappfia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.model.PartyItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.adapters.SelectPartyListAdapter;

import java.util.ArrayList;


public class SelectPartyDialog extends Dialog {

    onOKButtonListener listener = null;
    public interface onOKButtonListener {
        public void onOKButton(PartyItem partyItem, ArrayList<UserInfo> candidates);
    }

    private ListView                mListView;
    private SelectPartyListAdapter  mAdapter;
    private TextView                tvEmptyMessage;

    private ArrayList<UserInfo>     arrInvitingUsers;

    public SelectPartyDialog(Context context, ArrayList<UserInfo> invitingUsers, onOKButtonListener listener) {
        super(context);
        this.arrInvitingUsers = invitingUsers;
        this.listener = listener;
    }

    public SelectPartyDialog() {
        super(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_select_party);

        mAdapter    = new SelectPartyListAdapter(this, new ArrayList<PartyItem>());
        mListView   = (ListView) findViewById(R.id.lst_parties);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

        findViewById(R.id.lv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        initialize();
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private void initialize() {
        mAdapter.clear();
        for (PartyItem partyItem : FiaApplication.getMyParties()) {
            mAdapter.add(partyItem);
        }
        refreshControls();
    }

    private void refreshControls() {
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onSelectedParty(final PartyItem partyItem) {
        ArrayList<UserInfo> candidates = new ArrayList<>();
        ArrayList<InvitedItem> arrInvitedItems = partyItem.getInvitedItems();
        for (UserInfo userInfo : arrInvitingUsers) {
            boolean bExist = false;
            for (InvitedItem invitedItem : arrInvitedItems) {
                if (invitedItem.getUser().isEqual(userInfo)) {
                    bExist = true;
                    break;
                }
            }
            if (bExist)
                continue;

            candidates.add(userInfo);
            arrInvitedItems.add(new InvitedItem(userInfo, Constants.PARTY_STATE_INVITED, ""));
        }

        if (listener != null)
            listener.onOKButton(partyItem, candidates);

        dismiss();
    }
}
