package com.criterion.nativevitalio.Omron.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.criterion.nativevitalio.Omron.models.ReminderItem;
import com.criterion.nativevitalio.R;


import java.util.List;

/**
 * Created by Omron HealthCare Inc
 */

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.VersionViewHolder> {

    private final List<ReminderItem> mReminderItems;


    public SettingsListAdapter(List<ReminderItem> items) {
        this.mReminderItems = items;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_settings, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, final int position) {
        ReminderItem item = mReminderItems.get(position);

        versionViewHolder.tvReminderTime.setText(item.getHour() + ":" + item.getMinute());
        versionViewHolder.tvRepeat.setText(item.getDays());

    }

    @Override
    public int getItemCount() {
        return mReminderItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ReminderItem getItem(int position) {
        return mReminderItems.get(position);

    }


    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvReminderTime;
        private final TextView tvRepeat;

        VersionViewHolder(View view) {
            super(view);
            tvReminderTime = (TextView) view.findViewById(R.id.tv_reminder_time);
            tvRepeat = (TextView) view.findViewById(R.id.tv_repeat);

        }
    }
}
