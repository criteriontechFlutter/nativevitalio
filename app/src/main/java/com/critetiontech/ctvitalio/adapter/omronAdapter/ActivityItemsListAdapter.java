package com.critetiontech.ctvitalio.adapter.omronAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.UI.OmronActivity.ActivityDataActivity;
import com.critetiontech.ctvitalio.model.ActivityDataItem;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.R;


import java.util.List;

/**
 * Created by Omron HealthCare Inc
 */
public class ActivityItemsListAdapter extends RecyclerView.Adapter<ActivityItemsListAdapter.VersionViewHolder> {

    private final Context context;
    private List<ActivityDataItem> mActivityDataItemArrayList;
    private String localName;

    public ActivityItemsListAdapter(Context _context, List<ActivityDataItem> activityDataItemArrayList, String _localName) {
        this.context = _context;
        mActivityDataItemArrayList = activityDataItemArrayList;
        this.localName = _localName;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity_data, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, @SuppressLint("RecyclerView") final int position) {

        versionViewHolder.tvData.setText(mActivityDataItemArrayList.get(position).getName());
        versionViewHolder.linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toActivityData = new Intent(context, ActivityDataActivity.class);
                toActivityData.putExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_KEY, mActivityDataItemArrayList.get(position).getKey());
                toActivityData.putExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_TYPE, mActivityDataItemArrayList.get(position).getName());
                toActivityData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                context.startActivity(toActivityData);


            }
        });

    }

    @Override
    public int getItemCount() {
        if (mActivityDataItemArrayList == null) {
            return 0;
        }
        return (mActivityDataItemArrayList.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout linearLayout1;
        private final TextView tvData;

        VersionViewHolder(View view) {
            super(view);
            linearLayout1 = (RelativeLayout) view.findViewById(R.id.linearLayout1);
            tvData = (TextView) view.findViewById(R.id.tv_data);
        }
    }


}
