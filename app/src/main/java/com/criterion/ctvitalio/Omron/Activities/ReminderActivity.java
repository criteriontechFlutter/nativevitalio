package com.critetiontech.ctvitalio.Omron.Activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.Omron.adapter.ReminderListAdapter;
import com.critetiontech.ctvitalio.Omron.models.PeripheralDevice;
import com.critetiontech.ctvitalio.Omron.utility.PreferencesManager;
import com.critetiontech.ctvitalio.Omron.utility.sampleLog;
import com.critetiontech.ctvitalio.R;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Omron HealthCare Inc
 */

/******************************************************************************************************/
/************************ Reminder Functionality for Activity Device / HeartVue ***********************/
/******************************************************************************************************/

public class ReminderActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "ReminderActivity";
    private PreferencesManager preferencesManager;

    private TextView tvTimeFormat;
    private Switch swTimeFormat;

    private ReminderListAdapter reminderListAdapter;

    private PeripheralDevice peripheralDevice;

    public static final String ARG_DEVICE = "device";

    private int currentSelection = -1;
    private int mSelectedHour = 0;
    private int mSelectedMinute = 0;

    private boolean[] currentDaySelection = null;
    private JSONObject currentDaySelectionJSON = null;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        mContext = this;
        peripheralDevice = getIntent().getParcelableExtra(ARG_DEVICE);

        preferencesManager = new PreferencesManager(ReminderActivity.this);
        reminderListAdapter = new ReminderListAdapter(preferencesManager.getReminderList(peripheralDevice.getUuid()), new ReminderListAdapter.ReminderSelect() {
            @Override
            public void onTimeSelect(JSONObject reminder, int position) {
                currentSelection = position;
                showTimePicker();
            }

            @Override
            public void onRepeatSelect(JSONObject reminder, int position) {
                currentSelection = position;
                showRepeatSelector(false);
                reminderListAdapter.updateItemList(preferencesManager.getReminderList(peripheralDevice.getUuid()));
            }

            @Override
            public void onDeleteSelect(JSONObject reminder, int position) {
                preferencesManager.removeReminder(peripheralDevice.getUuid(), reminder);
                reminderListAdapter.updateItemList(preferencesManager.getReminderList(peripheralDevice.getUuid()));
            }
        });

        ImageView ivAddDevice = (ImageView) findViewById(R.id.iv_add_device);
        tvTimeFormat = (TextView) findViewById(R.id.tv_time_format);
        swTimeFormat = (Switch) findViewById(R.id.sw_time_format);
        RecyclerView rvSavedDevices = (RecyclerView) findViewById(R.id.rv_saved_devices);
        findViewById(R.id.btn_update).setOnClickListener(this);
        ivAddDevice.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(ReminderActivity.this, LinearLayoutManager.VERTICAL, false);
        rvSavedDevices.setLayoutManager(linearLayoutManager);
        rvSavedDevices.addItemDecoration(new DividerItemDecoration(ReminderActivity.this, DividerItemDecoration.VERTICAL));
        rvSavedDevices.setAdapter(reminderListAdapter);

        preferencesManager.getReminderFormat(peripheralDevice.getUuid());

        if (preferencesManager.getReminderFormat(peripheralDevice.getUuid()) == null) {
            preferencesManager.addReminderFormat(peripheralDevice.getUuid(), "24");
        }
        tvTimeFormat.setText(getResources().getString(R.string.time_format, preferencesManager.getReminderFormat(peripheralDevice.getUuid())));

        if (preferencesManager.getReminderFormat(peripheralDevice.getUuid()).equalsIgnoreCase("24")) {
            swTimeFormat.setChecked(false);
            reminderListAdapter.setIsTimeFormat24(true);
        } else {
            swTimeFormat.setChecked(true);
            reminderListAdapter.setIsTimeFormat24(false);
        }

        swTimeFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    preferencesManager.addReminderFormat(peripheralDevice.getUuid(), "12");
                    reminderListAdapter.setIsTimeFormat24(false);
                } else {
                    preferencesManager.addReminderFormat(peripheralDevice.getUuid(), "24");
                    reminderListAdapter.setIsTimeFormat24(true);
                }
                tvTimeFormat.setText(getResources().getString(R.string.time_format, preferencesManager.getReminderFormat(peripheralDevice.getUuid())));
            }
        });
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case  R.id.btn_update:
//                showProgressDialog(getString(R.string.update_reminder));
//                OmronPeripheralManagerConfig existingConfig = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).getConfiguration();
//                sampleLog.d("DeviceSettings", "Existing Settings - " + existingConfig.deviceSettings.toString());
//                ArrayList<HashMap> existingDeviceSettings =  (ArrayList<HashMap>)existingConfig.deviceSettings;
//
//                if (null != peripheralDevice) {
//                    ArrayList<HashMap> reminders = (ArrayList<HashMap>) preferencesManager.getReminderHashMap(peripheralDevice.getUuid());
//                    if (reminders.size() > 0) {
//
//                        HashMap<String, ArrayList> reminderSettings = new HashMap<>();
//                        reminderSettings.put(OmronConstants.OMRONDeviceAlarmSettingsKey, reminders);
//
//                        // Add new configuration to existing configuration
//                        existingDeviceSettings.add(reminderSettings);
//                    }
//
////
////                HashMap<String, String> timeSettings = new HashMap<String, String>();
////                timeSettings.put(OmronConstants.OMRONDeviceTimeSettings.FormatKey, preferencesManager.getReminderFormat(peripheralDevice.getUuid()));
////                HashMap<String, HashMap> deviceTimeSettings = new HashMap<>();
////                deviceTimeSettings.put(OmronConstants.OMRONDeviceTimeSettingsKey, timeSettings);
//
//                    HashMap<String, Object> dateFormatSettings = new HashMap<String, Object>();
//                    dateFormatSettings.put(OmronConstants.OMRONDeviceDateSettings.FormatKey, OmronConstants.OMRONDeviceDateFormat.DayMonth);
//                    HashMap<String, HashMap> dateSettings = new HashMap<>();
//                    dateSettings.put(OmronConstants.OMRONDeviceDateSettingsKey, dateFormatSettings);
//
//
//                    // Time Format
//                    HashMap<String, Object> timeFormatSettings = new HashMap<String, Object>();
//                    timeFormatSettings.put(OmronConstants.OMRONDeviceTimeSettings.FormatKey, OmronConstants.OMRONDeviceTimeFormat.Time24Hour);
//                    HashMap<String, HashMap> timeSettings = new HashMap<>();
//                    timeSettings.put(OmronConstants.OMRONDeviceTimeSettingsKey, timeFormatSettings);
//
//
//                    existingDeviceSettings.add(timeSettings);
//
//
//                    sampleLog.d("DeviceSettings", "New Settings - " + existingDeviceSettings);
//
//                    OmronPeripheralManagerConfig newConfig = new OmronPeripheralManagerConfig();
//                    existingConfig.deviceSettings = existingDeviceSettings;
//
//                    // Update Configuration
//                    OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).setConfiguration(newConfig);
//
//
//                    OmronPeripheral peripheral = new OmronPeripheral(peripheralDevice.getLocalName(), peripheralDevice.getUuid());
//
//                    //Call to update the settings
//                    OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).updatePeripheral(peripheral, new OmronPeripheralManagerUpdateListener() {
//                        @Override
//                        public void onUpdateCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                hideProgressDialog();
//                                if (resultInfo.isSuccess()) {
//                                    showMessage(getString(R.string.update_success), getString(R.string.update_settings_sucess));
//                                } else {
//                                    String detailInfo = resultInfo.getDetailInfo();
//                                    showMessage(getString(R.string.update_failed), "Error Code : " + detailInfo);
//                                }
//                            }
//                        });
//                        }
//                    });
//                }
//
//                break;
//            case R.id.iv_add_device:
//                if (reminderListAdapter.getItemCount() >= 5) {
//                    Toast.makeText(ReminderActivity.this, "You can add only 5 reminders", Toast.LENGTH_SHORT).show();
//                } else {
//                    currentSelection = -1;
//                    showTimePicker();
//                }
//                break;
//        }
    }

    private void showTimePicker() {
        int mHour = 0;
        int mMinute = 0;
        sampleLog.d("showTimePicker", "showTimePicker: ");

        if (currentSelection == -1) {
            Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            JSONObject jsonObject = reminderListAdapter.getItem(currentSelection);
            try {
                mHour = jsonObject.getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_HOUR);
                mMinute = jsonObject.getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_MINUTE);
            } catch (JSONException e) {
                sampleLog.e(TAG, e);
                e.printStackTrace();
            }
        }

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(ReminderActivity.this, new TimePickerDialog.OnTimeSetListener() {

            private int callCount = 0;

            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (callCount == 0) {
                    sampleLog.d("Time", selectedHour + ":" + selectedMinute);
                    mSelectedHour = selectedHour;
                    mSelectedMinute = selectedMinute;
                    showRepeatSelector(true);
                }
                callCount++;
            }
        }, mHour, mMinute, !swTimeFormat.isChecked());
        mTimePicker.show();
    }

    private void showRepeatSelector(boolean isFromTimePicker) {

        JSONObject jsonObject = null;

        if (!isFromTimePicker) {
            if (currentSelection == -1){
                return;}
            try {
                jsonObject = reminderListAdapter.getItem(currentSelection);
                mSelectedHour = jsonObject.getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_HOUR);
                mSelectedMinute = jsonObject.getInt(PreferencesManager.JSON_KEY_REMINDER_TIME_MINUTE);
            } catch (JSONException e) {
                sampleLog.e(TAG, e);
                e.printStackTrace();
            }
        }

        if (currentSelection == -1) {
            currentDaySelection = new boolean[]{true, true, true, true, true, true, true};
            currentDaySelectionJSON = new JSONObject();
            try {
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, 1);
                currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, 1);
            } catch (JSONException e) {
            }
        } else {
            currentDaySelection = new boolean[7];
            try {
                if (jsonObject == null) {
                    jsonObject = reminderListAdapter.getItem(currentSelection);
                }
                currentDaySelectionJSON = jsonObject.getJSONObject(PreferencesManager.JSON_KEY_REMINDER_REPEAT);
                currentDaySelection[0] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.SundayKey) == 1;
                currentDaySelection[1] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.MondayKey) == 1;
                currentDaySelection[2] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey) == 1;
                currentDaySelection[3] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey) == 1;
                currentDaySelection[4] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey) == 1;
                currentDaySelection[5] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.FridayKey) == 1;
                currentDaySelection[6] = currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey) == 1;
            } catch (JSONException e) {
                currentDaySelection = new boolean[]{true, true, true, true, true, true, true};
                currentDaySelectionJSON = new JSONObject();
                try {
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, 1);
                    currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, 1);
                } catch (JSONException e1) {
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Reminder")
                .setMultiChoiceItems(R.array.days, currentDaySelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        try {
                            switch (which) {
                                case 0:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 1:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 2:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 3:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 4:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 5:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                                case 6:
                                    if (isChecked) {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, 1);
                                        currentDaySelection[0] = true;
                                    } else {
                                        currentDaySelectionJSON.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, 0);
                                        currentDaySelection[0] = false;
                                    }
                                    break;
                            }
                        } catch (Exception e) {
                            sampleLog.e(TAG, e);
                            e.printStackTrace();
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean flagAtleastOneDaySelected = false;
                        for (boolean value : currentDaySelection) {
                            if (value) {
                                flagAtleastOneDaySelected = true;
                                break;
                            }
                        }

                        if (flagAtleastOneDaySelected) {
                            saveReminder();
                        }else {
                            Toast.makeText(ReminderActivity.this, "Select atleast one day", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    private void saveReminder() {
        JSONObject jsonObject = null;
        if (currentSelection == -1) {
            jsonObject = new JSONObject();
        } else {
            jsonObject = reminderListAdapter.getItem(currentSelection);
        }
        try {
            jsonObject.put(PreferencesManager.JSON_KEY_REMINDER_TIME_HOUR, mSelectedHour);
            jsonObject.put(PreferencesManager.JSON_KEY_REMINDER_TIME_MINUTE, mSelectedMinute);
            jsonObject.put(PreferencesManager.JSON_KEY_REMINDER_REPEAT, currentDaySelectionJSON);
        } catch (JSONException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
        preferencesManager.addReminder(peripheralDevice.getUuid(), jsonObject);
        reminderListAdapter.updateItemList(preferencesManager.getReminderList(peripheralDevice.getUuid()));
    }

    public void showMessage(String title, String message) {

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
