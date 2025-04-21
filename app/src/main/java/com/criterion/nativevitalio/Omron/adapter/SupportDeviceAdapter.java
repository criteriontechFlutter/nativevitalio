package com.critetiontech.ctvitalio.Omron.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import com.critetiontech.ctvitalio.R;


import java.util.List;
import java.util.Map;

/**
 * Created by Omron HealthCare Inc
 */

public class SupportDeviceAdapter extends BaseAdapter {

    private final Context context;

    private List<Map<String, String>> mDeviceList;

    public SupportDeviceAdapter(Context _context, List<Map<String, String>> deviceList) {
        this.context = _context;
        mDeviceList = deviceList;
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Map<String, String> item = getItem(position);
        View v;
        v = createContentView(convertView, inflator, item);
        return v;
    }

    private View createContentView(View convertView, LayoutInflater inflator, final Map<String, String> item) {

        View v = convertView;
        ViewHolder holder;

        if (convertView == null) {

            v = inflator.inflate(R.layout.list_item_device_listing, null);

            holder = new ViewHolder();
            holder.tvMdelName = v.findViewById(R.id.tv_model_name);
            holder.ivInfo = v.findViewById(R.id.iv_info);
            ImageView arrow = v.findViewById(R.id.iv_right_arrow);
            arrow.setVisibility(View.GONE);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tvMdelName.setText(item.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName) + "\n" + item.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
        if(item.get(OmronConstants.OMRONBLEConfigDevice.Thumbnail) != null) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier(item.get(OmronConstants.OMRONBLEConfigDevice.Thumbnail), "drawable", context.getPackageName());
            holder.ivInfo.setImageResource(resourceId);
        }else {
            holder.ivInfo.setImageResource(R.drawable.baseline_info_outline_24);
        }
        return v;
    }

    public static class ViewHolder {

        private TextView tvMdelName;
        private ImageView ivInfo;
    }

}
