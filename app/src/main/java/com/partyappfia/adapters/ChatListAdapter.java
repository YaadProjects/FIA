package com.partyappfia.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.activities.ChatActivity;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.fragments.HomeFragment;
import com.partyappfia.model.ChatItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class ChatListAdapter extends ArrayAdapter<ChatItem> {

    private ChatActivity mChatActivity;

    public ChatListAdapter(ChatActivity chatActivity, List<ChatItem> objects) {
        super(chatActivity, R.layout.listitem_chat, objects);
        mChatActivity = chatActivity;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mChatActivity).inflate(R.layout.listitem_chat, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final ChatItem item = getItem(position);
        if (item != null) {

            boolean bSendMsg = item.getUser().isEqual(FiaApplication.getMyUserInfo());

            holder.lvLeftPane.setVisibility(bSendMsg ? View.GONE : View.VISIBLE);
            holder.lvRightPane.setVisibility(bSendMsg ? View.VISIBLE : View.GONE);

            if (bSendMsg) {
                BitmapHelper.drawImageUrlFromPicasso(mChatActivity, item.getUser().getPhotoUrl(), R.drawable.avatar_mark, holder.ivRightAvatar);
                holder.tvRightMessage.setText(item.getMessage());
                holder.tvRightMessage.setText(item.isRemoved() ? mChatActivity.getString(R.string.removed_message) : item.getMessage());
                holder.lvRightFrame.setAlpha(item.isRemoved() ? 0.4f : 1.f);
                holder.tvRightMessage.setTextColor(item.isRemoved() ? Color.BLACK : Color.WHITE);
                holder.ivRightTrash.setVisibility(item.isRemoved() ? View.VISIBLE : View.GONE);
                holder.lvRightFrame.setLongClickable(!item.isRemoved());

                if (!item.isRemoved()) {
                    holder.lvRightFrame.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mChatActivity.onRightPaneLongClicked(item);
                            return true;
                        }
                    });
                }

            } else {
                BitmapHelper.drawImageUrlFromPicasso(mChatActivity, item.getUser().getPhotoUrl(), R.drawable.avatar_mark, holder.ivLeftAvatar);
                holder.tvLeftMessage.setText(item.getMessage());
                holder.tvLeftMessage.setText(item.isRemoved() ? mChatActivity.getString(R.string.removed_message) : item.getMessage());
                holder.lvLeftFrame.setAlpha(item.isRemoved() ? 0.4f : 1.f);
                holder.tvLeftMessage.setTextColor(item.isRemoved() ? Color.BLACK : Color.WHITE);
                holder.ivLeftTrash.setVisibility(item.isRemoved() ? View.VISIBLE : View.GONE);
            }
        }

        return view;
    }

    public ArrayList<ChatItem> getList() {
        ArrayList<ChatItem> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            ChatItem item = getItem(i);
            list.add(item);
        }

        return list;
    }

    private class ViewHolder {
        public LinearLayout     lvLeftPane;
        public LinearLayout     lvLeftFrame;
        public CircleImageView  ivLeftAvatar;
        public TextView         tvLeftMessage;
        public ImageView        ivLeftTrash;

        public LinearLayout     lvRightPane;
        public LinearLayout     lvRightFrame;
        public CircleImageView  ivRightAvatar;
        public TextView         tvRightMessage;
        public ImageView        ivRightTrash;

        public ViewHolder(View rootView) {
            lvLeftPane      = (LinearLayout) rootView.findViewById(R.id.lv_leftpane);
            lvLeftFrame     = (LinearLayout) rootView.findViewById(R.id.lv_leftframe);
            ivLeftAvatar    = (CircleImageView) rootView.findViewById(R.id.iv_left_avatar);
            tvLeftMessage   = (TextView) rootView.findViewById(R.id.tv_left_message);
            ivLeftTrash     = (ImageView) rootView.findViewById(R.id.iv_left_trash);

            lvRightPane     = (LinearLayout) rootView.findViewById(R.id.lv_rightpane);
            lvRightFrame    = (LinearLayout) rootView.findViewById(R.id.lv_rightframe);
            ivRightAvatar   = (CircleImageView) rootView.findViewById(R.id.iv_right_avatar);
            tvRightMessage  = (TextView) rootView.findViewById(R.id.tv_right_message);
            ivRightTrash    = (ImageView) rootView.findViewById(R.id.iv_right_trash);
        }
    }
}
