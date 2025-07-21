package com.critetiontech.ctvitalio.adapter.omronAdapter;

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

import com.critetiontech.ctvitalio.UI.OmronActivity.OmronConnectedDeviceList;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.R;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;


import java.util.List;
import java.util.Map;

/**
 * Created by Omron HealthCare Inc
 */

public class ConnectedDeviceAdapter extends BaseAdapter {

    private final Context context;

    private final List<Map<String, String>> mDeviceList;
    private final OmronConnectedDeviceList mOmronConnectedDeviceList;

    public ConnectedDeviceAdapter(Context _context, List<Map<String, String>> deviceList) {
        this.context = _context;
        mDeviceList = deviceList;
        mOmronConnectedDeviceList = (OmronConnectedDeviceList) context;
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
            holder.tvModelName = v.findViewById(R.id.tv_model_name);
            holder.ivInfo = v.findViewById(R.id.iv_info);
            holder.llBg = v.findViewById(R.id.ll_bg);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        String Info = item.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName) + "(" + item.get(OmronConstants.OMRONBLEConfigDevice.Identifier) + ")";
        String UserNo = item.get(Constants.deviceInfoKeys.KEY_SELECTED_USER);
        if(!UserNo.equals("0")){
            Info += "\n" + item.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME) + "\nUser " + UserNo;
        }
        holder.tvModelName.setText(Info);

        if(item.get(OmronConstants.OMRONBLEConfigDevice.Thumbnail) != null) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier(item.get(OmronConstants.OMRONBLEConfigDevice.Thumbnail), "drawable", context.getPackageName());
            holder.ivInfo.setImageResource(resourceId);
        }else {
            holder.ivInfo.setImageResource(R.drawable.baseline_info_outline_24);
        }

        holder.llBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOmronConnectedDeviceList.selectDevice(item);
            }
        });
        holder.llBg.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                mOmronConnectedDeviceList.showDeleteMenu(item);
                return  true;
            }

        });
        return v;
    }

    public static class ViewHolder {

        private TextView tvModelName;
        private LinearLayout llBg;
        private ImageView ivInfo;
    }

}
