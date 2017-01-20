package com.partyappfia.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.activities.NotificationActivity;
import com.partyappfia.config.Constants;
import com.partyappfia.model.NotificationItem;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;
import com.partyappfia.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class NotificationListAdapter extends ArrayAdapter<NotificationItem> {

    private NotificationActivity mNotificationActivity;

    public NotificationListAdapter(NotificationActivity activity, List<NotificationItem> objects) {
        super(activity, R.layout.listitem_notification, objects);
        mNotificationActivity = activity;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mNotificationActivity).inflate(R.layout.listitem_notification, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final NotificationItem item = getItem(position);
        if (item != null) {
            BitmapHelper.drawImageUrlFromPicasso(mNotificationActivity, item.getSenderUser().getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
            holder.tvUsername.setText(item.getSenderUser().getUsername());
            holder.tvTime.setText(TimeHelper.getDiffTime(item.getUploadTime()));
            holder.tvMessage.setText(item.getMessage());

            if (item.getNotificationType() != Constants.NOTIFICATION_TYPE_PARTY_INVITED || item.getPartyItem() == null) {
                holder.lvPane.setEnabled(false);
                holder.lvPane.setClickable(false);
                holder.lvPane.setBackgroundColor(mNotificationActivity.getResources().getColor(R.color.colorLightGray));
            } else {
                holder.lvPane.setEnabled(true);
                holder.lvPane.setClickable(true);
                holder.lvPane.setBackgroundColor(Color.WHITE);
                holder.lvPane.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNotificationActivity.onSelectedNotification(item);
                    }
                });
            }
        }

        return view;
    }

    public ArrayList<NotificationItem> getList() {
        ArrayList<NotificationItem> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            NotificationItem item = getItem(i);
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
