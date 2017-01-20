package com.partyappfia.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.config.Constants;
import com.partyappfia.fragments.MessageFragment;
import com.partyappfia.model.MessageItem;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;
import com.partyappfia.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class MessageListAdapter extends ArrayAdapter<MessageItem> {

    private MessageFragment mMessageFragment;

    public MessageListAdapter(MessageFragment fragment, List<MessageItem> objects) {
        super(fragment.getContext(), R.layout.listitem_message, objects);
        mMessageFragment = fragment;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mMessageFragment.getContext()).inflate(R.layout.listitem_message, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final MessageItem item = getItem(position);
        if (item != null) {
            String message = item.getMessage();
            boolean bRemoved = false;
            if (message.contains(Constants.MESSAGE_REMOVED_MARK) || message.contains(Constants.MESSAGE_UPDATED_MARK)) {
                message = "(Removed a message)";
                bRemoved = true;
            }

            BitmapHelper.drawImageUrlFromPicasso(mMessageFragment.getContext(), item.getUser().getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
            holder.tvUsername.setText(item.getUser().getUsername());
            holder.tvMessage.setText(message);
            holder.tvMessage.setTextColor(bRemoved ? mMessageFragment.getResources().getColor(R.color.colorRed) : Color.BLACK);
            holder.tvTime.setText(TimeHelper.getDiffTime(item.getUploadTime()));

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMessageFragment.onSelectedMessage(item);
                }
            });
        }

        return view;
    }

    public ArrayList<MessageItem> getList() {
        ArrayList<MessageItem> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            MessageItem item = getItem(i);
            list.add(item);
        }

        return list;
    }

    private class ViewHolder {
        public View             lvPane;
        public CircleImageView  ivAvatar;
        public TextView         tvUsername;
        public TextView         tvTime;
        public TextView         tvMessage;

        public ViewHolder(View rootView) {
            lvPane      = rootView.findViewById(R.id.lv_pane);
            ivAvatar    = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
            tvUsername  = (TextView) rootView.findViewById(R.id.tv_username);
            tvTime      = (TextView) rootView.findViewById(R.id.tv_time);
            tvMessage   = (TextView) rootView.findViewById(R.id.tv_message);
        }
    }
}
