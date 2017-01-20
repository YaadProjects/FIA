package com.partyappfia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.partyappfia.R;
import com.partyappfia.dialogs.SelectPartyDialog;
import com.partyappfia.model.PartyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chau Thai on 4/12/16.
 */
public class SelectPartyListAdapter extends ArrayAdapter<PartyItem> {

    private SelectPartyDialog mSelectPartyDialog;

    public SelectPartyListAdapter(SelectPartyDialog dialog, List<PartyItem> objects) {
        super(dialog.getContext(), R.layout.listitem_party, objects);
        mSelectPartyDialog = dialog;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mSelectPartyDialog.getContext()).inflate(R.layout.listitem_party, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final PartyItem item = getItem(position);
        if (item != null) {
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
                    mSelectPartyDialog.onSelectedParty(item);
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
