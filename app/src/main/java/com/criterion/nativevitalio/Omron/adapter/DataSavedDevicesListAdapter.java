package com.critetiontech.ctvitalio.Omron.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.critetiontech.ctvitalio.Omron.utility.PreferencesManager;
import com.critetiontech.ctvitalio.Omron.utility.sampleLog;
import com.critetiontech.ctvitalio.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Omron HealthCare Inc
 */

public class DataSavedDevicesListAdapter extends RecyclerView.Adapter<DataSavedDevicesListAdapter.VersionViewHolder> {
    private final static String TAG = "DataSavedDevicesListAdapter";
    private JSONArray deviceList;
    private DeviceItemSelect mSavedItemSelect;
    private DeviceDelete mDeviceDelete;

    public DataSavedDevicesListAdapter(JSONArray _deviceList, DeviceItemSelect savedItemSelect ,DeviceDelete deviceDelete) {
        this.deviceList = _deviceList;
        this.mSavedItemSelect = savedItemSelect;
        this.mDeviceDelete = deviceDelete;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_data_saved_devices, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, final int position) {
        try {
            versionViewHolder.tvLocalName.setText(deviceList.getJSONObject(position).getString(PreferencesManager.JSON_KEY_DEVICE_LOCAL_NAME));
            versionViewHolder.tvModelName.setText(deviceList.getJSONObject(position).getString(PreferencesManager.JSON_KEY_DEVICE_MODEL_NAME));
        } catch (JSONException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (deviceList == null) {
            return 0;
        }
        return (deviceList.length());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public JSONObject getItem(int position) {
        try {
            return deviceList.getJSONObject(position);
        } catch (JSONException e) {
            return null;
        }
    }

    public void updateItemList(JSONArray _deviceList) {
        this.deviceList=_deviceList;
        notifyDataSetChanged();
    }

    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final LinearLayout llDetails;
        private final TextView tvModelName;
        private final TextView tvLocalName;

        VersionViewHolder(View view) {
            super(view);
            llDetails = (LinearLayout) view.findViewById(R.id.ll_details);
            tvModelName = (TextView) view.findViewById(R.id.tv_model_name);
            tvLocalName = (TextView) view.findViewById(R.id.tv_local_name);

            llDetails.setOnClickListener(this);
            llDetails.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ll_details) {
                try {
                    mSavedItemSelect.onItemSelect(deviceList.getJSONObject(getAdapterPosition()).getString(PreferencesManager.JSON_KEY_DEVICE_LOCAL_NAME), deviceList.getJSONObject(getAdapterPosition()).getString(PreferencesManager.JSON_KEY_DEVICE_IDENTIFIER), deviceList.getJSONObject(getAdapterPosition()).getInt(PreferencesManager.JSON_KEY_DEVICE_CATEGORY), getAdapterPosition());
                } catch (JSONException e) {
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // Processing when item is long pressed
            mDeviceDelete.onItemDelete(view, getAdapterPosition());
            return true;
        }
    }
    public interface DeviceItemSelect {
        void onItemSelect(String localName,String identifier, Integer category, int position);
    }

    public interface DeviceDelete {
        void onItemDelete(View view, int position);
    }

}
