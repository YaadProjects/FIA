package com.partyappfia.adapters;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.partyappfia.R;
import com.partyappfia.config.Constants;
import com.partyappfia.fragments.HomeFragment;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class HomeUserListAdapter extends ArrayAdapter<UserInfo> {

    private HomeFragment            mHomeFragment = null;
    private final ViewBinderHelper  binderHelper;

    public HomeUserListAdapter(HomeFragment fragment, List<UserInfo> objects) {
        super(fragment.getContext(), R.layout.listitem_home_user, objects);
        mHomeFragment = fragment;
        binderHelper  = new ViewBinderHelper();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mHomeFragment.getContext()).inflate(R.layout.listitem_home_user, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final UserInfo item = getItem(position);
        if (item != null) {
            binderHelper.bind(holder.swipeLayout, item.getId());

            BitmapHelper.drawImageUrlFromPicasso(mHomeFragment.getContext(), item.getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
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

            holder.flDeletePane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHomeFragment.onDeleteUserButtonClicked(item);
                }
            });

            holder.swipeLayout.close(false);

            // save width and height of rect view
            holder.swipeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    //Remove it here unless you want to get this callback for EVERY
                    //layout pass, which can get you into infinite loops if you ever
                    //modify the layout from within this method.
                    holder.swipeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    holder.swipeLayout.requestLayout();
                }
            });

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHomeFragment.onSelectedUser(item);
                }
            });

            holder.lvPane.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onCheckButtonChanged(item, !item.isSelected());
                    return true;
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
        mHomeFragment.refreshControls();
    }

    private class ViewHolder {
        public SwipeRevealLayout    swipeLayout;
        public View                 flDeletePane;
        public View                 lvPane;
        public CheckBox             chkSelect;
        public CircleImageView      ivAvatar;
        public TextView             tvUsername;
        public TextView             tvPhone;
        public RadioGroup           rgRating;

        public ViewHolder(View rootView) {
            swipeLayout     = (SwipeRevealLayout) rootView.findViewById(R.id.swipe_layout);
            flDeletePane    = rootView.findViewById(R.id.fl_delete_pane);
            lvPane          = rootView.findViewById(R.id.lv_pane);
            chkSelect       = (CheckBox)rootView.findViewById(R.id.chk_select);
            ivAvatar        = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
            tvUsername      = (TextView) rootView.findViewById(R.id.tv_username);
            tvPhone         = (TextView) rootView.findViewById(R.id.tv_phone);
            rgRating        = (RadioGroup) rootView.findViewById(R.id.rg_rating);
        }
    }
}
