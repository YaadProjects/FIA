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
import com.partyappfia.model.UserInfo;
import com.partyappfia.adapters.SingleSelectUserListAdapter;

import java.util.ArrayList;

/**
 * Created by Great Summit on 11/14/2015.
 */
public class SingleSelectUserDialog extends Dialog {

    private onOKButtonListener listener = null;
    public interface onOKButtonListener {
        public void onOKButton(UserInfo selectedUserInfo);
    }

    private ListView mListView;
    private SingleSelectUserListAdapter mAdapter;
    private TextView tvEmptyMessage;

    private ArrayList<UserInfo> arrUsers = new ArrayList<>();

    public SingleSelectUserDialog(Context context, onOKButtonListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_single_select_user);

        for (UserInfo userInfo : FiaApplication.getMyUserInfo().getFriends()) {
            UserInfo newUser = new UserInfo(userInfo);
            newUser.setSelected(false);
            arrUsers.add(userInfo);
        }

        mAdapter = new SingleSelectUserListAdapter(this, new ArrayList<UserInfo>());
        mListView = (ListView) findViewById(R.id.lst_users);
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

    private void initialize() {
        if (mAdapter != null)
            mAdapter.clear();

        for (UserInfo userInfo : arrUsers) {
            userInfo.setSelected(false);
            mAdapter.add(userInfo);
        }

        refreshControls();
    }

    public void refreshControls() {
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onUserSelected(UserInfo entity) {
        if (listener != null)
            listener.onOKButton(entity);
        dismiss();
    }

    @Override
    public void onBackPressed() {
        cancel();
    }
}
