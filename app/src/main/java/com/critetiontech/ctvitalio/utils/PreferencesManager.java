package com.critetiontech.ctvitalio.utils;

/**
 * Created by Omron HealthCare Inc
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.critetiontech.ctvitalio.model.PeripheralDevice;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PreferencesManager {
    private final static String TAG = "PreferencesManager";
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "OmronConnectivitySamplePref";

    // All Shared Preferences Keys
    private static final String KEY_DEVICE_LIST = "DeviceList";
    private static final String KEY_REMINDER_LIST = "ReminderList";
    private static final String KEY_REMINDER_TIME_FORMAT = "ReminderTimeFormat";
    private static final String KEY_DATA_STORED_DEVICES_LIST = "DataStoredDevicesList";
    public static final String KEY_PARTNER_KEY_VALUE= "partnerKey";

    public static final String JSON_KEY_REMINDER_ID = "creationTimeStamp";
    public static final String JSON_KEY_REMINDER_TIME_HOUR = "reminderTimeHour";
    public static final String JSON_KEY_REMINDER_TIME_MINUTE = "reminderTimeMinute";
    public static final String JSON_KEY_REMINDER_REPEAT = "reminderRepeat";

    public static final String JSON_KEY_DEVICE_LOCAL_NAME = "deviceLocalName";
    public static final String JSON_KEY_DEVICE_CATEGORY = "deviceCategory";
    public static final String JSON_KEY_DEVICE_MODEL_NAME= "deviceModelName";
    public static final String JSON_KEY_DEVICE_IDENTIFIER= "deviceIdentifier";


    // Constructor
    public PreferencesManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /*
    * Save a list of devices into preferences
     */
    public void setList(List<PeripheralDevice> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(KEY_DEVICE_LIST, json);
        editor.commit();
    }

    /*
    * Add a device into already saved list of devices
     */
    public void addPeripheralDevice(PeripheralDevice productToAdd) {
        List<PeripheralDevice> list = getSavedDeviceList();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(productToAdd);
        setList(list);
    }

    /*
    *Get the list of devices stored in preferences
     */
    public List<PeripheralDevice> getSavedDeviceList() {
        Gson gson = new Gson();
        List<PeripheralDevice> deviceArrayList;
        String deviceListJson = pref.getString(KEY_DEVICE_LIST, null);
        if (deviceListJson == null) {
            return null;
        }
        Type type = new TypeToken<List<PeripheralDevice>>() {
        }.getType();
        deviceArrayList = gson.fromJson(deviceListJson, type);
        return deviceArrayList;
    }

    public void addReminder(String macID, JSONObject jsonObject){
        String reminderListJson = pref.getString(KEY_REMINDER_LIST, null);
        JSONObject reminderJSON = null;
        if (reminderListJson == null) {
            reminderJSON = new JSONObject();
        } else {
            try {
                reminderJSON = new JSONObject(reminderListJson);
            } catch (JSONException e) {
                reminderJSON = new JSONObject();
            }
        }
        JSONArray reminderArray;

        try {
            reminderArray = reminderJSON.getJSONArray(macID);
        } catch (JSONException e) {
            reminderArray = new JSONArray();
        }

        //TODO duplication check
        try {
            if (!jsonObject.has(JSON_KEY_REMINDER_ID)) {
                jsonObject.put(JSON_KEY_REMINDER_ID, System.currentTimeMillis());
            }
        } catch (JSONException e) {
        }

        int currentPosition = -1;
        if (reminderArray.length() > 0){
            for (int i = 0; i < reminderArray.length(); i++) {
                JSONObject object = null;
                try {
                    object = reminderArray.getJSONObject(i);
                    if (jsonObject.getString(JSON_KEY_REMINDER_ID).equalsIgnoreCase(object.getString(JSON_KEY_REMINDER_ID))) {
                        currentPosition = i;
                        break;
                    }
                } catch (JSONException e) {
                    sampleLog.e(TAG, e);
                    e.printStackTrace();
                }
            }
        }

        try {
            if (currentPosition != -1) {
                reminderArray.put(currentPosition, jsonObject);
            }else {
                reminderArray.put(jsonObject);
            }
        } catch (JSONException e) {
            reminderArray.put(jsonObject);
        }
        try {
            reminderJSON.put(macID, reminderArray);
        } catch (JSONException e) {
        }
        editor.putString(KEY_REMINDER_LIST, reminderJSON.toString());
        editor.commit();
    }

    public List<HashMap> getReminderHashMap(String macID) {

        ArrayList<HashMap> reminders = new ArrayList<>();

        String reminderListJson = pref.getString(KEY_REMINDER_LIST, null);
        JSONObject reminderJSON = null;
        if (reminderListJson == null) {
            reminderJSON = new JSONObject();
        } else {
            try {
                reminderJSON = new JSONObject(reminderListJson);
            } catch (JSONException e) {
                reminderJSON = new JSONObject();
            }
        }
        JSONArray reminderArray;

        try {
            reminderArray = reminderJSON.getJSONArray(macID);
        } catch (JSONException e) {
            reminderArray = new JSONArray();
        }

        if (reminderArray.length() > 0) {
            for (int i = 0; i < reminderArray.length(); i++) {
                JSONObject object = null;
                HashMap<String, HashMap> alarm1 = new HashMap<>();
                try {
                    object = reminderArray.getJSONObject(i);
                    HashMap<String, String> time = new HashMap<>();
                    time.put(OmronConstants.OMRONDeviceAlarmSettings.HourKey, String.format("%02d", object.getInt(JSON_KEY_REMINDER_TIME_HOUR)));
                    time.put(OmronConstants.OMRONDeviceAlarmSettings.MinuteKey, String.format("%02d", object.getInt(JSON_KEY_REMINDER_TIME_MINUTE)));
                    HashMap<String, String> days = new HashMap<>();

                    try {
                        JSONObject currentDaySelectionJSON = object.getJSONObject(PreferencesManager.JSON_KEY_REMINDER_REPEAT);
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.SundayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.MondayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.FridayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, "0");
                        }
                        if (currentDaySelectionJSON.getInt(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey) == 1) {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, "1");
                        } else {
                            days.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, "0");
                        }
                    } catch (JSONException e) {
                    }


                    alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.TimeKey, time);
                    alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.DaysKey, days);
                } catch (JSONException e) {
                    sampleLog.e(TAG, e);
                    e.printStackTrace();
                }

                reminders.add(alarm1);
            }
        }
        return reminders;

    }

    public void removeReminder(String macID, JSONObject jsonObject){
        String reminderListJson = pref.getString(KEY_REMINDER_LIST, null);
        JSONObject reminderJSON = null;
        if (reminderListJson == null) {
            reminderJSON = new JSONObject();
        } else {
            try {
                reminderJSON = new JSONObject(reminderListJson);
            } catch (JSONException e) {
                reminderJSON = new JSONObject();
            }
        }
        JSONArray reminderArray;

        try {
            reminderArray = reminderJSON.getJSONArray(macID);
        } catch (JSONException e) {
            reminderArray = new JSONArray();
        }

        //TODO duplication check
        try {
            if (!jsonObject.has(JSON_KEY_REMINDER_ID)) {
                jsonObject.put(JSON_KEY_REMINDER_ID, System.currentTimeMillis());
            }
        } catch (JSONException e) {
        }

        JSONArray reminderArrayTemp = new JSONArray();
        if (reminderArray.length() > 0) {
            for (int i = 0; i < reminderArray.length(); i++) {
                JSONObject object = null;
                try {
                    object = reminderArray.getJSONObject(i);
                    if (!jsonObject.getString(JSON_KEY_REMINDER_ID).equalsIgnoreCase(object.getString(JSON_KEY_REMINDER_ID))) {
                        reminderArrayTemp.put(object);
                    }
                } catch (JSONException e) {
                    sampleLog.e(TAG, e);
                    e.printStackTrace();
                }

            }
        }
        try {
            reminderJSON.put(macID, reminderArrayTemp);
        } catch (JSONException e) {
        }
        editor.putString(KEY_REMINDER_LIST, reminderJSON.toString());
        editor.commit();
    }

    public JSONArray getReminderList(String macID){
        String reminderListJson = pref.getString(KEY_REMINDER_LIST, null);
        try {
            JSONObject reminderJSON = new JSONObject(reminderListJson);
            return reminderJSON.getJSONArray(macID);
        } catch (Exception e) {
            return null;
        }
    }


    public void addReminderFormat(String macID, String format){
        String reminderformatJsonString = pref.getString(KEY_REMINDER_TIME_FORMAT, null);
        JSONObject reminderFormatJSON = null;
        if (reminderformatJsonString == null) {
            reminderFormatJSON = new JSONObject();
        } else {
            try {
                reminderFormatJSON = new JSONObject(reminderformatJsonString);
            } catch (Exception e) {
                reminderFormatJSON = new JSONObject();
            }
        }

        try {
            reminderFormatJSON.put(macID, format);
        } catch (JSONException e) {
        }
        editor.putString(KEY_REMINDER_TIME_FORMAT, reminderFormatJSON.toString());
        editor.commit();
    }

    public String getReminderFormat(String macID){
        String reminderListJson = pref.getString(KEY_REMINDER_TIME_FORMAT, null);
        try {
            JSONObject reminderJSON = new JSONObject(reminderListJson);
            return reminderJSON.getString(macID);
        } catch (Exception e) {
            return null;
        }
    }
    public void addDataStoredDeviceList(String localName, int category, String modelName, String identifier) {
        String reminderformatJsonString = pref.getString(KEY_DATA_STORED_DEVICES_LIST, null);
        JSONObject reminderFormatJSON;
        if (reminderformatJsonString == null) {
            reminderFormatJSON = new JSONObject();
        } else {
            try {
                reminderFormatJSON = new JSONObject(reminderformatJsonString);
            } catch (Exception e) {
                reminderFormatJSON = new JSONObject();
            }
        }

        JSONObject object = new JSONObject();
        try {
            object.put(JSON_KEY_DEVICE_LOCAL_NAME, localName);
            object.put(JSON_KEY_DEVICE_MODEL_NAME, modelName);
            object.put(JSON_KEY_DEVICE_IDENTIFIER, identifier);
            object.put(JSON_KEY_DEVICE_CATEGORY, category);
        } catch (JSONException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
        //If the same localName already exists, delete it
        if (reminderFormatJSON.has(localName)) {
            reminderFormatJSON.remove(localName);
        }
        try {
            JSONObject tempJSON = new JSONObject();
            //Added new data to the top
            tempJSON.put(localName, object);
            //Add existing data later
            Iterator<String> keys = reminderFormatJSON.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                tempJSON.put(key, reminderFormatJSON.get(key));
            }
            //Separate the data that is temporarily saved into the original variable
            reminderFormatJSON = tempJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editor.putString(KEY_DATA_STORED_DEVICES_LIST, reminderFormatJSON.toString());
        editor.apply();
    }
    public void removeDataFromStoredDeviceList(String localName) {
        String reminderformatJsonString = pref.getString(KEY_DATA_STORED_DEVICES_LIST, null);
        if (reminderformatJsonString != null) {
            try {
                JSONObject reminderFormatJSON = new JSONObject(reminderformatJsonString);
                reminderFormatJSON.remove(localName);
                editor.putString(KEY_DATA_STORED_DEVICES_LIST, reminderFormatJSON.toString());
                editor.commit();
            } catch (JSONException e) {
                sampleLog.e(TAG, e);
                e.printStackTrace();
            }
        }
    }
    public void clearStoredDeviceList() {
        editor.remove(KEY_DATA_STORED_DEVICES_LIST);
        editor.commit();
    }
    public int getDataStoredDeviceCategory(String localName){
        String reminderListJson = pref.getString(KEY_DATA_STORED_DEVICES_LIST, null);
        try {
            JSONObject reminderJSON = new JSONObject(reminderListJson);
            return reminderJSON.getJSONObject(localName).getInt(JSON_KEY_DEVICE_CATEGORY);
        } catch (Exception e) {
            return -1;
        }
    }

    public JSONArray getDataStoredDeviceList(){
        String reminderListJson = pref.getString(KEY_DATA_STORED_DEVICES_LIST, null);
        JSONArray dataStoredDevices = new JSONArray();
        try {
            JSONObject reminderJSON = new JSONObject(reminderListJson);
            Iterator<String> iter = reminderJSON.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    dataStoredDevices.put(reminderJSON.getJSONObject(key));
                } catch (JSONException e) {
                    // Something went wrong!
                }
            }
            return dataStoredDevices;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clear all details
     */
    public void clear() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    public void savePartnerKey(String partnerKey) {
        editor.putString(KEY_PARTNER_KEY_VALUE, partnerKey);
        editor.apply();

    }

    public String getPartnerKey() {
        if (pref != null) {
            return pref.getString(KEY_PARTNER_KEY_VALUE,"A68C0CB6-A612-4CA4-9143-6136F4AC0751");
        } else {
            return "";
        }
    }
}