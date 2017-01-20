package com.partyappfia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.partyappfia.R;
import com.partyappfia.dialogs.MultiSelectUserDialog;
import com.partyappfia.config.Constants;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class MultiSelectUserListAdapter extends ArrayAdapter<UserInfo> {
    private MultiSelectUserDialog mMultiSelectUserDialog = null;

    public MultiSelectUserListAdapter(MultiSelectUserDialog dialog, List<UserInfo> objects) {
        super(dialog.getContext(), R.layout.listitem_multi_select_user, objects);
        mMultiSelectUserDialog = dialog;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mMultiSelectUserDialog.getContext()).inflate(R.layout.listitem_multi_select_user, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final UserInfo item = getItem(position);
        if (item != null) {
            BitmapHelper.drawImageUrlFromPicasso(mMultiSelectUserDialog.getContext(), item.getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
            holder.tvUsername.setText(item.getUsername());
            holder.tvPhone.setText(item.getPhone());

            switch (item.getRating()) {
                case Constants.LEVEL_LOW:       holder.rgRating.check(R.id.rdo_low);    break;
                case Constants.LEVEL_MIDDLE:    holder.rgRating.check(R.id.rdo_middle); break;
                case Constants.LEVEL_HIGH:      holder.rgRating.check(R.id.rdo_high);   break;
                default:                        holder.rgRating.clearCheck();           break;
            }

            holder.chkSelect.setChecked(item.isSelected());
            holder.chkSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onCheckButtonChanged(item, isChecked);
                }
            });

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCheckButtonChanged(item, !item.isSelected());
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

    private void onCheckButtonChanged(UserInfo item, boolean isChecked) {
        item.setSelected(isChecked);
        mMultiSelectUserDialog.refreshControls();
    }

    private class ViewHolder {
        public View             lvPane;
        public CheckBox         chkSelect;
        public CircleImageView  ivAvatar;
        public TextView         tvUsername;
        public TextView         tvPhone;
        public RadioGroup       rgRating;

        public ViewHolder(View rootView) {
            lvPane      = rootView.findViewById(R.id.lv_pane);
            chkSelect   = (CheckBox)rootView.findViewById(R.id.chk_select);
            ivAvatar    = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
            tvUsername  = (TextView) rootView.findViewById(R.id.tv_username);
            tvPhone     = (TextView) rootView.findViewById(R.id.tv_phone);
            rgRating    = (RadioGroup) rootView.findViewById(R.id.rg_rating);
        }
    }
}
