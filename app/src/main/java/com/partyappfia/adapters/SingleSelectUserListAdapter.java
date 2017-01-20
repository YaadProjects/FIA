package com.partyappfia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.config.Constants;
import com.partyappfia.dialogs.SingleSelectUserDialog;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class SingleSelectUserListAdapter extends ArrayAdapter<UserInfo> {

    private SingleSelectUserDialog mDialog;

    public SingleSelectUserListAdapter(SingleSelectUserDialog dialog, List<UserInfo> objects) {
        super(dialog.getContext(), R.layout.listitem_single_select_user, objects);
        mDialog = dialog;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mDialog.getContext()).inflate(R.layout.listitem_single_select_user, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final UserInfo item = getItem(position);
        if (item != null) {
            BitmapHelper.drawImageUrlFromPicasso(mDialog.getContext(), item.getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
            holder.tvUsername.setText(item.getUsername());
            holder.tvPhone.setText(item.getPhone());

            switch (item.getRating()) {
                case Constants.LEVEL_LOW:       holder.rgRating.check(R.id.rdo_low);    break;
                case Constants.LEVEL_MIDDLE:    holder.rgRating.check(R.id.rdo_middle); break;
                case Constants.LEVEL_HIGH:      holder.rgRating.check(R.id.rdo_high);   break;
                default:                        holder.rgRating.clearCheck();           break;
            }

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.onUserSelected(item);
                }
            });
        }

        return view;
    }

    public ArrayList<UserInfo> getList() {
        ArrayList<UserInfo> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            UserInfo item = getItem(i);
            list.add(item);
        }

        return list;
    }

    private class ViewHolder {
        public View             lvPane;
        public CircleImageView  ivAvatar;
        public TextView         tvUsername;
        public TextView         tvPhone;
        public RadioGroup       rgRating;

        public ViewHolder(View rootView) {
            lvPane      = rootView.findViewById(R.id.lv_pane);
            ivAvatar    = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
            tvUsername  = (TextView) rootView.findViewById(R.id.tv_username);
            tvPhone     = (TextView) rootView.findViewById(R.id.tv_phone);
            rgRating    = (RadioGroup) rootView.findViewById(R.id.rg_rating);
        }
    }
}
