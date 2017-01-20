package com.partyappfia.adapters;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.partyappfia.R;
import com.partyappfia.activities.PartyDetailActivity;
import com.partyappfia.config.Constants;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.CircleImageView;
import com.partyappfia.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class PartyUserListAdapter extends ArrayAdapter<InvitedItem> {

    private PartyDetailActivity     mPartyDetailActivity;
    private final ViewBinderHelper  binderHelper;
    private boolean                 bLockSwipe;

    public PartyUserListAdapter(PartyDetailActivity activity, List<InvitedItem> objects) {
        super(activity, R.layout.listitem_party_user, objects);

        mPartyDetailActivity    = activity;
        binderHelper            = new ViewBinderHelper();
        bLockSwipe              = false;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mPartyDetailActivity).inflate(R.layout.listitem_party_user, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final InvitedItem item = getItem(position);
        if (item != null) {
            binderHelper.bind(holder.swipeLayout, item.getUser().getId());
            if (bLockSwipe)
                binderHelper.lockSwipe(item.getUser().getId());
            else
                binderHelper.unlockSwipe(item.getUser().getId());

            BitmapHelper.drawImageUrlFromPicasso(mPartyDetailActivity, item.getUser().getPhotoUrl(), R.drawable.avatar_mark, holder.ivAvatar);
            holder.tvUsername.setText(item.getUser().getUsername());
            holder.tvPhone.setText(item.getUser().getPhone());
            if (mPartyDetailActivity.isMyParty()) {
                holder.tvTime.setVisibility(item.getCheckTime().isEmpty() ? View.GONE : View.VISIBLE);
                holder.tvTime.setText(TimeHelper.getDiffTime(item.getCheckTime()));
                holder.tvAccepted.setVisibility(item.getState() == Constants.PARTY_STATE_INVITED ? View.GONE : View.VISIBLE);
                holder.tvAccepted.setText(item.getStateString());
                holder.tvAccepted.setTextColor(item.getStateColor(mPartyDetailActivity));
            } else {
                holder.tvTime.setVisibility(View.GONE);
                holder.tvAccepted.setVisibility(View.GONE);
            }

            switch (item.getUser().getRating()) {
                case Constants.LEVEL_LOW:       holder.rgRating.check(R.id.rdo_low);    break;
                case Constants.LEVEL_MIDDLE:    holder.rgRating.check(R.id.rdo_middle); break;
                case Constants.LEVEL_HIGH:      holder.rgRating.check(R.id.rdo_high);   break;
                default:                        holder.rgRating.clearCheck();           break;
            }

            holder.flDeletePane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPartyDetailActivity.onDeleteUserButtonClicked(item);
                }
            });

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPartyDetailActivity.onSelectedUser(item.getUser());
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
        }

        return view;
    }

    public ArrayList<InvitedItem> getList() {
        ArrayList<InvitedItem> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            InvitedItem item = getItem(i);
            list.add(item);
        }

        return list;
    }

    public void setLockSwipe(boolean bLock) {
        bLockSwipe = bLock;
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder {
        public SwipeRevealLayout    swipeLayout;
        public View                 flDeletePane;
        public View                 lvPane;
        public CircleImageView      ivAvatar;
        public TextView             tvUsername;
        public TextView             tvPhone;
        public TextView             tvTime;
        public TextView             tvAccepted;
        public RadioGroup           rgRating;

        public ViewHolder(View rootView) {
            swipeLayout     = (SwipeRevealLayout) rootView.findViewById(R.id.swipe_layout);
            flDeletePane    = rootView.findViewById(R.id.fl_delete_pane);
            lvPane          = rootView.findViewById(R.id.lv_pane);
            ivAvatar        = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
            tvUsername      = (TextView) rootView.findViewById(R.id.tv_username);
            tvPhone         = (TextView) rootView.findViewById(R.id.tv_phone);
            tvTime          = (TextView) rootView.findViewById(R.id.tv_time);
            tvAccepted      = (TextView) rootView.findViewById(R.id.tv_accepted);
            rgRating        = (RadioGroup) rootView.findViewById(R.id.rg_rating);
        }
    }
}
