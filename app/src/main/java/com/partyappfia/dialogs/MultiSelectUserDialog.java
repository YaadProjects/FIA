package com.partyappfia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.model.UserInfo;
import com.partyappfia.adapters.MultiSelectUserListAdapter;

import java.util.ArrayList;

/**
 * Created by Great Summit on 1/14/2016.
 */
public class MultiSelectUserDialog extends Dialog {

    private onOKButtonListener listener = null;
    public interface onOKButtonListener {
        public void onOKButton(ArrayList<UserInfo> users);
    }

    private View        lvSelectAll;
    private ImageView   ivSelectAll;
    private TextView    tvSelectAll;
    private EditText    evSearch;
    private View        lvSearchCancel;

    private ListView    mListView;
    private MultiSelectUserListAdapter mAdapter;
    private TextView    tvEmptyMessage;

    private boolean     bSelected = false;

    private ArrayList<UserInfo> arrUsers = new ArrayList<>();
    private boolean     bOnlyMyFriends = false;

    public MultiSelectUserDialog(Context context, boolean bOnlyMyFriends, onOKButtonListener listener) {
        super(context);
        this.bOnlyMyFriends = bOnlyMyFriends;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_multi_select_user);

        for (UserInfo userInfo : bOnlyMyFriends ? FiaApplication.getMyUserInfo().getFriends() : FiaApplication.getUsers()) {
            UserInfo newUser = new UserInfo(userInfo);
            newUser.setSelected(false);
            arrUsers.add(userInfo);
        }

        lvSelectAll = findViewById(R.id.lv_select_all);
        ivSelectAll = (ImageView) findViewById(R.id.iv_select_all);
        tvSelectAll = (TextView) findViewById(R.id.tv_select_all);
        evSearch        = (EditText) findViewById(R.id.ev_search);
        lvSearchCancel  = findViewById(R.id.lv_search_cancel);

        mAdapter = new MultiSelectUserListAdapter(this, new ArrayList<UserInfo>());
        mListView = (ListView) findViewById(R.id.lst_users);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

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
                lvSearchCancel.setVisibility(searchText.isEmpty() ? View.GONE : View.VISIBLE);
                search(searchText, false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lvSearchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evSearch.setText("");
            }
        });

        findViewById(R.id.lv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onOKButton(getSelectedUsers());
                dismiss();
            }
        });

        findViewById(R.id.lv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        initialize();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private void initialize() {
        search("", true);
    }

    public void search(String searchText, boolean bCleanSelecting) {
        searchText = searchText.toLowerCase().trim();

        if (mAdapter != null)
            mAdapter.clear();

        for (UserInfo userInfo : arrUsers) {
            if (!searchText.isEmpty()) {
                if (!userInfo.getUsername().contains(searchText))
                    continue;
            } else {
                if (bCleanSelecting)
                    userInfo.setSelected(false);
            }

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

        tvSelectAll.setText(String.format("   %d/%d selected", selectedCount, mAdapter.getCount()));
        ivSelectAll.setImageResource(selectedCount > 0 && selectedCount == mAdapter.getCount() ? R.drawable.checkbox_selected : R.drawable.checkbox_white);

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
}
