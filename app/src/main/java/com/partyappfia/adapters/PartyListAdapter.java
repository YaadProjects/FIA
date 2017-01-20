package com.partyappfia.adapters;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.partyappfia.R;
import com.partyappfia.fragments.PartyFragment;
import com.partyappfia.model.PartyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class PartyListAdapter extends ArrayAdapter<PartyItem> {

    private final ViewBinderHelper  binderHelper;
    private PartyFragment           mPartyFragment = null;
    private boolean                 bLockSwipe;

    public PartyListAdapter(PartyFragment fragment, List<PartyItem> objects) {
        super(fragment.getContext(), R.layout.listitem_party, objects);

        mPartyFragment  = fragment;
        binderHelper    = new ViewBinderHelper();
        bLockSwipe      = false;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mPartyFragment.getContext()).inflate(R.layout.listitem_party, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final PartyItem item = getItem(position);
        if (item != null) {
            binderHelper.bind(holder.swipeLayout, item.getId());
            if (bLockSwipe)
                binderHelper.lockSwipe(item.getId());
            else
                binderHelper.unlockSwipe(item.getId());

            holder.tvTitle.setText(item.getTitle());
            holder.tvComment.setText(item.getComment());
            holder.tvTime.setVisibility(item.getExpireTimeByLocal().isEmpty() ? View.GONE : View.VISIBLE);
            holder.tvTime.setText(item.getExpireTimeByLocal());

            if (item.getInvitedItems().size() >= 100) {
                holder.ivLevel.setImageResource(R.drawable.party_level_high);
            } else if (item.getInvitedItems().size() >= 50) {
                holder.ivLevel.setImageResource(R.drawable.party_level_middle);
            } else {
                holder.ivLevel.setImageResource(R.drawable.party_level_low);
            }

            holder.lvPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPartyFragment.onSelectedParty(item);
                }
            });

            holder.flDeletePane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPartyFragment.onDeleteItemClicked(item);
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

    public ArrayList<PartyItem> getList() {
        ArrayList<PartyItem> list = new ArrayList<>();

        for (int i = 0; i < getCount(); i ++) {
            PartyItem item = getItem(i);
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
        public TextView             tvTitle;
        public TextView             tvComment;
        public ImageView            ivLevel;
        public TextView             tvTime;

        public ViewHolder(View rootView) {
            swipeLayout     = (SwipeRevealLayout) rootView.findViewById(R.id.swipe_layout);
            flDeletePane    = rootView.findViewById(R.id.fl_delete_pane);
            lvPane          = rootView.findViewById(R.id.lv_pane);
            tvTitle         = (TextView) rootView.findViewById(R.id.tv_title);
            tvComment       = (TextView) rootView.findViewById(R.id.tv_comment);
            ivLevel         = (ImageView) rootView.findViewById(R.id.iv_level);
            tvTime          = (TextView) rootView.findViewById(R.id.tv_time);
        }
    }
}
