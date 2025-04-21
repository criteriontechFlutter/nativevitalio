package com.critetiontech.ctvitalio.Omron.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.critetiontech.ctvitalio.Omron.utility.PreferencesManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.critetiontech.ctvitalio.R;
import com.critetiontech.ctvitalio.Omron.utility.sampleLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Omron HealthCare Inc
 */

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.VersionViewHolder> {
    private final static String TAG = "ReminderListAdapter";
    private ReminderSelect mSavedItemSelect;
    private Boolean isTimeFormat24 = true;
    private JSONArray peripheralDevices;

    private static final String STR_P2D = "%02d";

    public ReminderListAdapter(JSONArray _peripheralDevices, ReminderSelect savedItemSelect) {
        this.peripheralDevices = _peripheralDevices;
        this.mSavedItemSelect = savedItemSelect;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reminder, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, final int position) {
        try {
            int hour = peripheralDevices.getJSONObject(position).getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_HOUR);
            int minute = peripheralDevices.getJSONObject(position).getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_MINUTE);
            versionViewHolder.tvReminderTime.setText(getDisplayTime(hour, minute));
            versionViewHolder.tvRepeat.setText(getDisplayReminder(peripheralDevices.getJSONObject(position).getJSONObject(PreferencesManager.JSON_KEY_REMINDER_REPEAT)));
        } catch (JSONException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (peripheralDevices == null) {
            return 0;
        }
        return (peripheralDevices.length());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public JSONObject getItem(int position) {
        try {
            return peripheralDevices.getJSONObject(position);
        } catch (JSONException e) {
            return null;
        }
    }

    public void updateItemList(JSONArray _peripheralDevices) {
        this.peripheralDevices = _peripheralDevices;
        notifyDataSetChanged();
    }

    public void setIsTimeFormat24(boolean _isTimeFormat24) {
        this.isTimeFormat24 = _isTimeFormat24;
        notifyDataSetChanged();
    }

    private String getDisplayTime(int hourOfDay, int minute) {

        if(isTimeFormat24){
            return String.format(STR_P2D, hourOfDay)
                    + ":" + String.format(STR_P2D,minute) + " ";
        }else{
            String aMpM = "AM";
            if (hourOfDay > 11) {
                aMpM = "PM";
            }

            int currentHour;
            if (hourOfDay > 11) {
                currentHour = hourOfDay - 12;
            } else {
                currentHour = hourOfDay;
            }

            if(currentHour==0) {
                currentHour = 12;
            }
            return String.format(STR_P2D,currentHour)
                    + ":" + String.format(STR_P2D,minute) + " " + aMpM;
        }

    }

    private String getDisplayReminder(JSONObject object) {
        String displayString = "";
        try {
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.SundayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Sun";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.MondayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Mon";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Tue";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Wed";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Thu";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.FridayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Fri";
            }
            if (object.getInt(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey) == 1) {
                if (displayString.trim().length() > 0) {
                    displayString = displayString + ", ";
                }
                displayString=displayString+"Sat";
            }
        } catch (JSONException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
        return displayString;
    }

    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvReminderTime;
        private final TextView tvRepeat;
        private final ImageView ivRemove;

        VersionViewHolder(View view) {
            super(view);
            tvReminderTime = (TextView) view.findViewById(R.id.tv_reminder_time);
            tvRepeat = (TextView) view.findViewById(R.id.tv_repeat);
            ivRemove = (ImageView) view.findViewById(R.id.iv_remove);

            tvReminderTime.setOnClickListener(this);
            tvRepeat.setOnClickListener(this);
            ivRemove.setOnClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.tv_reminder_time:
//                    try {
//                        mSavedItemSelect.onTimeSelect(peripheralDevices.getJSONObject(getAdapterPosition()), getAdapterPosition());
//                    } catch (JSONException e) {
//                    }
//                    break;
//                case R.id.tv_repeat:
//                    try {
//                        mSavedItemSelect.onRepeatSelect(peripheralDevices.getJSONObject(getAdapterPosition()), getAdapterPosition());
//                    } catch (JSONException e) {
//                    }
//                    break;
//                case R.id.iv_remove:
//                    try {
//                        mSavedItemSelect.onDeleteSelect(peripheralDevices.getJSONObject(getAdapterPosition()), getAdapterPosition());
//                    } catch (JSONException e) {
//                    }
//                    break;
//            }
        }
    }

    public interface ReminderSelect {
        void onTimeSelect(JSONObject peripheralDevice, int position);

        void onRepeatSelect(JSONObject peripheralDevice, int position);

        void onDeleteSelect(JSONObject peripheralDevice, int position);
    }
}
