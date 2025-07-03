package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.model.PairingDeviceData;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.utils.PreferencesManager;
import com.critetiontech.ctvitalio.utils.Utilities;
import com.critetiontech.ctvitalio.utils.sampleLog;
import com.critetiontech.ctvitalio.R;
import com.critetiontech.ctvitalio.utils.MyApplication;
import com.intuit.sdp.BuildConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectStateListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDataTransferListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerStopScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityMainActivity extends BaseActivity {

    private Context mContext;
    private static final String TAG = "OmronSampleApp";
    private OmronPeripheral mSelectedPeripheral;
    private String mSequenceNoString;
    private final List<Integer> selectedUsers = new ArrayList<>();
    private TextView mTvDeviceLocalName, mTvDeviceUuid, mTvStatusLabel, mTvErrorCode, mTvErrorDesc;
    private ProgressBar mProgressBar;
    private Button transferBtn;
    private PreferencesManager preferencesManager = null;

    private Map<String, String> device = null;

    private final Integer connectStatus_Idle = 0;
    private final Integer connectStatus_Scanning = 1;
    private final Integer connectStatus_Connecting = 2;
    private Integer connectStatus = connectStatus_Idle;
    private static final String STR_DEVICE_INFO = "Device Information : ";
    private static final String STR_CONNECTING = "Connecting...";
    private boolean isReceiverRegistered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(this);
        }
        // Selected device
        device = (HashMap<String, String>) getIntent().getSerializableExtra(Constants.extraKeys.KEY_SELECTED_DEVICE);
        String user = device.get(Constants.deviceInfoKeys.KEY_SELECTED_USER);
        if(user != null) {
            selectedUsers.add(Integer.parseInt(user));
        }
        mSelectedPeripheral = PairingDeviceData.changePeripheralObject(device);
        initViews();
        initClickListeners();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Activity Tracker - Testing of notification
        Utilities.scheduleNotification(Utilities.getNotification("Test Notification"), 5000);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            mContext.unregisterReceiver(mMessageReceiver);
        }
    }
    /**
     * Permissions for activity device
     */
    private void requestPermissions() {
        // Activity Tracker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1);
        }
    }
    /**
     * Configure library functionalities
     */
    private void startOmronPeripheralManager() {

        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).getConfiguration();
        sampleLog.d(TAG,"Library Identifier : " + peripheralConfig.getLibraryIdentifier());

        // Filter device to scan and connect (optional)
        if (device != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID) != null) {
            // Add item
            List<HashMap<String, String>> filterDevices = new ArrayList<>();
            filterDevices.add((HashMap<String, String>)device);
            peripheralConfig.deviceFilters = filterDevices;
        }

        ArrayList<HashMap> deviceSettings = new ArrayList<>();

        // Personal device settings (optional)
        deviceSettings = (ArrayList<HashMap>) getPersonalSettings(deviceSettings);

        // Scan settings (optional)
        deviceSettings = (ArrayList<HashMap>) getScanSettings(deviceSettings);

        peripheralConfig.deviceSettings = deviceSettings;
        // Set Scan timeout interval (optional)
        peripheralConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT;
        // Set User Hash Id (mandatory)
        peripheralConfig.userHashId = "<email_address_of_user>"; // Set logged in user email
        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).setConfiguration(peripheralConfig);

        //Initialize the connection process.
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).startManager();

        // Notification Listener for BLE State Change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(mMessageReceiver,
                    new IntentFilter(OmronConstants.OMRONBLEBluetoothStateNotification),RECEIVER_NOT_EXPORTED);
        }else{
            mContext.registerReceiver(mMessageReceiver,
                    new IntentFilter(OmronConstants.OMRONBLEBluetoothStateNotification));
        }
        //Track instances of BroadcastReceiver.
        isReceiverRegistered = true;
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            int status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0);
            if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateUnknown) {

                sampleLog.d(TAG, "Bluetooth is in unknown state");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOff) {

                sampleLog.d(TAG, "Bluetooth is currently powered off");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOn) {

                sampleLog.d(TAG, "Bluetooth is currently powered on");
            }
        }
    };

    private void setStateChanges() {
        // Listen to Device state changes using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).onConnectStateChange(new OmronPeripheralManagerConnectStateListener() {

            @Override
            public void onConnectStateChange(final int state) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String status = "-";
                        if (state == OmronConstants.OMRONBLEConnectionState.CONNECTING) {
                            connectStatus = connectStatus_Connecting;
                            status = STR_CONNECTING;
                        } else if (state == OmronConstants.OMRONBLEConnectionState.CONNECTED) {
                            status = "Connected";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTING) {
                            connectStatus = connectStatus_Idle;
                            status = "Disconnecting...";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTED) {
                            status = "Disconnected";
                            enableDisableButton(true);
                        }
                        setStatus(status);
                    }
                });
            }
        });
    }
    private void disconnectDevice() {
        if(connectStatus == connectStatus_Scanning) {
            OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).stopScanPeripherals(new OmronPeripheralManagerStopScanListener() {
                @Override
                public void onStopScanCompleted(final OmronErrorInfo resultInfo) {}
            });
        }else if(connectStatus == connectStatus_Connecting) {
            // Disconnect device using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).disconnectPeripheral(mSelectedPeripheral, new OmronPeripheralManagerDisconnectListener() {
                @Override
                public void onDisconnectCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ActivityMainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        connectStatus = connectStatus_Idle;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectDevice();
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
    /*
        OmronPeripheralManager Transfer Function
     */

    private void transferData() {

        if (mSelectedPeripheral == null) {
            mTvErrorDesc.setText("Device Not Paired");
            return;
        }

        resetErrorMessage();
        enableDisableButton(false);
        resetVitalDataResult();

        startOmronPeripheralManager();
        // Set State Change Listener
        setStateChanges();
        connectStatus = connectStatus_Scanning;
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).startDataTransferFromPeripheral(mSelectedPeripheral, selectedUsers, true, new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
                if (resultInfo.isSuccess() && peripheral != null) {

                    HashMap<String, String> deviceInformation = (HashMap<String, String>) peripheral.getDeviceInformation();
                    sampleLog.d(TAG, STR_DEVICE_INFO + deviceInformation);

                    mSelectedPeripheral = peripheral; // Saving for Transfer Function

                    // Save Device to List
                    // To change based on data available
                    preferencesManager.addDataStoredDeviceList(device.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName(), device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));

                    ArrayList<HashMap<String, Object>> vitalDataList = null;
                    Map<String, Object> personalSettingsItem = null;
                    // Setting Data
                    Object objectItem = peripheral.getDeviceSettingsWithUser(selectedUsers.get(0));
                    if(!(objectItem instanceof OmronErrorInfo)){
                        personalSettingsItem = (Map<String, Object>) objectItem;
                    }
                    // Get vital data for previously selected user using OmronPeripheral
                    Object output = peripheral.getVitalData();
                    if (output instanceof OmronErrorInfo) {
                        //output is OmronErrorInfo = Data reception failure
                        final OmronErrorInfo errorInfo = (OmronErrorInfo) output;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTvErrorCode.setText(errorInfo.getDetailInfo());
                                mTvErrorDesc.setText(errorInfo.getMessageInfo());
                                enableDisableButton(true);
                            }
                        });
                    } else {
                        //output not OmronErrorInfo = If data can be received
                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;
                        if (vitalData != null) {
                            HashMap<String, String> deviceInfo = (HashMap<String, String>) peripheral.getDeviceInformation();
                            // VitalData Data
                            vitalDataList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataBloodPressureKey);
                            insertVitalDataToDB(vitalDataList, deviceInfo);
                        }
                        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).endDataTransferFromPeripheral(new OmronPeripheralManagerDataTransferListener() {
                            @Override
                            public void onDataTransferCompleted(final OmronPeripheral peripheral, final OmronErrorInfo errorInfo) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }
                        });
                    }
                    showVitalDataResult(vitalDataList, personalSettingsItem,mSelectedPeripheral.getDeviceInformation());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setStatus("-");
                            mTvErrorCode.setText(resultInfo.getDetailInfo());
                            mTvErrorDesc.setText(resultInfo.getMessageInfo());
                            enableDisableButton(true);
                        }
                    });
                }
            }

        });
    }
    private List<HashMap> getScanSettings(List<HashMap> deviceSettings) {

        // Scan Settings
        HashMap<String, Object> ScanModeSettings = new HashMap<>();
        HashMap<String, HashMap> ScanSettings = new HashMap<>();
        ScanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence);
        ScanSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, ScanModeSettings);

        deviceSettings.add(ScanSettings);

        return deviceSettings;
    }
    private TextView addLayoutToScrollView(LinearLayout parentLayout , String tagText) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layoutView = inflater.inflate(R.layout.list_item_display, parentLayout, false);

        TextView tagTextView = layoutView.findViewById(R.id.tagText);
        tagTextView.setText(tagText);

        TextView setValueTextView = layoutView.findViewById(R.id.setValue);

        parentLayout.addView(layoutView);
        return setValueTextView;
    }
    private void setTextView(TextView view, Map<String, Object> dataList, String key, String addText){
        Object objectData = dataList.get(key);
        if (objectData != null) {
            view.setText(objectData + addText);
        }
    }
    // UI Functions
    //Common UI
    private void enableDisableButton(boolean enable) {
        transferBtn.setEnabled(enable);
        mProgressBar.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void setStatus(String statusMessage) {
        mTvStatusLabel.setText(statusMessage);
    }

    private void resetErrorMessage() {
        mTvErrorCode.setText("-");
        mTvErrorDesc.setText("-");
        mTvStatusLabel.setText("-");
    }
    private void initCommonViews(){
        setContentView(R.layout.activity_data_transfer);
        mProgressBar = findViewById(R.id.pb_transfer);
        mTvDeviceLocalName = findViewById(R.id.tv_device_name);
        mTvDeviceLocalName.setText(mSelectedPeripheral.getLocalName());
        mTvDeviceUuid = findViewById(R.id.tv_device_uuid);
        mTvDeviceUuid.setText(mSelectedPeripheral.getUuid());
        mTvStatusLabel = findViewById(R.id.tv_status_value);
        mTvErrorCode = findViewById(R.id.tv_error_value);
        mTvErrorDesc = findViewById(R.id.tv_error_desc);
    }
    //Individual UI
    private TextView mTvTImeStamp, mTvUserSelected, mTvSeqNum;
    private TextView mTvSystolic, mTvDiastolic, mTvPulseRate;
    private TextView mTvDateOfBirth, mTvBatteryRem;
    private TextView mTvTruReadEnable, mTvTruReadInterval;
    private boolean isDevelopmentTest = false;
    private void initViews() {
        initCommonViews();
        if (BuildConfig.BUILD_TYPE.equals("devtest")) {
            //Enable display only during Library development testing
            isDevelopmentTest = true;
        }
        Resources res = mContext.getResources();
        //Title bar Text
        TextView textView;
        textView = findViewById(R.id.tvLabel);
        textView.setText(device.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName) + " - Connection Status");
        textView = findViewById(R.id.device_info);
        textView.setText(res.getString(R.string.blood_pressure));

        LinearLayout parentLayout2 = findViewById(R.id.linear_layout2);
        mTvTImeStamp = addLayoutToScrollView(parentLayout2,res.getString(R.string.time_stamp));
        mTvSystolic = addLayoutToScrollView(parentLayout2,res.getString(R.string.systolic));
        mTvDiastolic = addLayoutToScrollView(parentLayout2,res.getString(R.string.diastolic));
        mTvPulseRate = addLayoutToScrollView(parentLayout2,res.getString(R.string.pulse_rate));
        mTvUserSelected = addLayoutToScrollView(parentLayout2,res.getString(R.string.user_selected));
        if(isDevelopmentTest) {
            mTvSeqNum = addLayoutToScrollView(parentLayout2, res.getString(R.string.sequence_number));
        }

        LinearLayout parentLayout3 = findViewById(R.id.linear_layout3);
        mTvDateOfBirth = addLayoutToScrollView(parentLayout3,res.getString(R.string.date_of_birth));
        mTvBatteryRem = addLayoutToScrollView(parentLayout3,res.getString(R.string.battery_rem));
        if(isDevelopmentTest){
            mTvTruReadEnable = addLayoutToScrollView(parentLayout3,res.getString(R.string.tru_read_enable));
            mTvTruReadInterval = addLayoutToScrollView(parentLayout3,res.getString(R.string.tru_read_interval));
            mTvSeqNum.setText(mSequenceNoString != null ? mSequenceNoString : "-");
        }
        mTvUserSelected.setText("User " +  android.text.TextUtils.join(",",selectedUsers));
        transferBtn = findViewById(R.id.btn_transfer);
    }

    private void initClickListeners() {
        //Data transfer
        transferBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                transferData();
            }
        });

        //Open Vital data activity
        findViewById(R.id.iv_vital_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toVitalData = new Intent(ActivityMainActivity.this, DataListingActivity.class);
                toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, mSelectedPeripheral.getLocalName());
                toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
                startActivity(toVitalData);
            }
        });
    }
    private void showVitalDataResult(final List<HashMap<String, Object>> vitalData, final Map<String, Object> personalSettingsItem, final Map<String, String> deviceInfo) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                resetVitalDataResult();

                if (vitalData.size() == 0) {

                    mTvErrorDesc.setText("No New readings transferred");

                } else {
                    HashMap<String, Object> vitalDataItem = vitalData.get(vitalData.size() - 1);

                    mTvErrorDesc.setText("-");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((Long) vitalDataItem.get(OmronConstants.OMRONVitalData.StartDateKey));
                    mTvTImeStamp.setText(format.format(calendar.getTime()));
                    setTextView(mTvSystolic,vitalDataItem,OmronConstants.OMRONVitalData.SystolicKey,"\t mmHg");
                    setTextView(mTvDiastolic,vitalDataItem,OmronConstants.OMRONVitalData.DiastolicKey,"\t mmHg");
                    setTextView(mTvPulseRate,vitalDataItem,OmronConstants.OMRONVitalData.PulseKey,"\t bpm");

                    Object objectData;
                    objectData = vitalDataItem.get(OmronConstants.OMRONVitalData.UserIdKey);
                    if (objectData != null) {
                        mTvUserSelected.setText("User " + objectData);
                    }

                    objectData = vitalDataItem.get(OmronConstants.OMRONVitalData.SequenceKey);
                    if(objectData != null){
                        mSequenceNoString= String.valueOf(objectData);
                        if(isDevelopmentTest) {
                            mTvSeqNum.setText(mSequenceNoString);
                        }
                    }
                    PairingDeviceData.addPairingDataToDB(mSelectedPeripheral,mSequenceNoString);
                }
                Map<String,Object> _info = new HashMap<String, Object>(deviceInfo);
                setTextView(mTvBatteryRem,_info,OmronConstants.OMRONDeviceInformation.BatteryRemainingKey,"%");
                if(personalSettingsItem != null){
                    String stringDateOfBirth = personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey).toString();
                    Date date = getDateOfBirthDateType(stringDateOfBirth,"yyyyMMdd");
                    stringDateOfBirth = getDate(date);
                    mTvDateOfBirth.setText(stringDateOfBirth);
                    if(isDevelopmentTest) {
                        HashMap<String, Object> bloodPressurePersonalItem = (HashMap<String, Object>) personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey);
                        if (bloodPressurePersonalItem != null) {
                            setTextView(mTvTruReadEnable,bloodPressurePersonalItem,OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey,"");
                            setTextView(mTvTruReadInterval,bloodPressurePersonalItem,OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey,"");
                        }
                    }
                }
            }
        });

    }
    private void resetVitalDataResult() {
        mTvTImeStamp.setText("-");
        mTvSystolic.setText("-");
        mTvDiastolic.setText("-");
        mTvPulseRate .setText("-");
        mTvDateOfBirth.setText("-");
        mTvBatteryRem.setText("-");
        if(isDevelopmentTest){
            mTvTruReadEnable.setText("-");
            mTvTruReadInterval.setText("-");
            mTvSeqNum.setText("-");
        }
        mTvUserSelected.setText("-");
    }
    //individual setting
    private List<HashMap> getPersonalSettings(List<HashMap> deviceSettings) {

        HashMap<String, String> settingsModel = new HashMap<String, String>();
        settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, personalData.getHeight());
        settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserWeightKey, personalData.getWeight());
        settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserStrideKey, personalData.getStride());
        settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.TargetSleepKey, "120");
        settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.TargetStepsKey, "2000");

        HashMap<String, HashMap> userSettings = new HashMap<>();
        userSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settingsModel);

        // Notification settings
        ArrayList<String> notificationsAvailable = new ArrayList<>();
        notificationsAvailable.add("android.intent.action.PHONE_STATE");
        notificationsAvailable.add("com.google.android.gm");
        notificationsAvailable.add("android.provider.Telephony.SMS_RECEIVED");
        notificationsAvailable.add(" com.critetiontech.ctvitalio");
        HashMap<String, Object> notificationSettings = new HashMap<String, Object>();
        notificationSettings.put(OmronConstants.OMRONDeviceNotificationSettingsKey, notificationsAvailable);

        // Time Format
        HashMap<String, Object> timeFormatSettings = new HashMap<String, Object>();
        timeFormatSettings.put(OmronConstants.OMRONDeviceTimeSettings.FormatKey, OmronConstants.OMRONDeviceTimeFormat.Time12Hour);
        HashMap<String, HashMap> timeSettings = new HashMap<>();
        timeSettings.put(OmronConstants.OMRONDeviceTimeSettingsKey, timeFormatSettings);


        // Sleep Settings
        HashMap<String, Object> sleepTimeSettings = new HashMap<String, Object>();
        sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.AutomaticKey, OmronConstants.OMRONDeviceSleepAutomatic.Off);
        sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.StartTimeKey, "19");
        sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.StopTimeKey, "20");
        HashMap<String, HashMap> sleepSettings = new HashMap<>();
        sleepSettings.put(OmronConstants.OMRONDeviceSleepSettingsKey, sleepTimeSettings);


        // Alarm Settings
        // Alarm 1 Time
        HashMap<String, Object> alarmTime1 = new HashMap<String, Object>();
        alarmTime1.put(OmronConstants.OMRONDeviceAlarmSettings.HourKey, "15");
        alarmTime1.put(OmronConstants.OMRONDeviceAlarmSettings.MinuteKey, "33");
        // Alarm 1 Day (SUN-SAT)
        HashMap<String, Object> alarmDays1 = new HashMap<String, Object>();
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, OmronConstants.OMRONDeviceAlarmStatus.On);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        HashMap<String, Object> alarm1 = new HashMap<>();
        alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.DaysKey, alarmDays1);
        alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.TimeKey, alarmTime1);
        alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.TypeKey, OmronConstants.OMRONDeviceAlarmType.Measure);


        // Alarm 2 Time
        HashMap<String, Object> alarmTime2 = new HashMap<String, Object>();
        alarmTime2.put(OmronConstants.OMRONDeviceAlarmSettings.HourKey, "15");
        alarmTime2.put(OmronConstants.OMRONDeviceAlarmSettings.MinuteKey, "34");
        // Alarm 2 Day (SUN-SAT)
        HashMap<String, Object> alarmDays2 = new HashMap<String, Object>();
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, OmronConstants.OMRONDeviceAlarmStatus.On);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
        HashMap<String, Object> alarm2 = new HashMap<>();
        alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.DaysKey, alarmDays2);
        alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.TimeKey, alarmTime2);
        alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.TypeKey, OmronConstants.OMRONDeviceAlarmType.Medication);

        // Add Alarm1, Alarm2, Alarm3 to List
        ArrayList<HashMap> alarms = new ArrayList<>();
        alarms.add(alarm1);
        alarms.add(alarm2);
        HashMap<String, Object> alarmSettings = new HashMap<>();
        alarmSettings.put(OmronConstants.OMRONDeviceAlarmSettingsKey, alarms);


        // Notification enable settings
        HashMap<String, Object> notificationEnableSettings = new HashMap<String, Object>();
        notificationEnableSettings.put(OmronConstants.OMRONDeviceNotificationStatusKey, OmronConstants.OMRONDeviceNotificationStatus.On);
        HashMap<String, HashMap> notificationStatusSettings = new HashMap<>();
        notificationStatusSettings.put(OmronConstants.OMRONDeviceNotificationEnableSettingsKey, notificationEnableSettings);


        deviceSettings.add(userSettings);
        deviceSettings.add(notificationSettings);
        deviceSettings.add(alarmSettings);
        deviceSettings.add(timeSettings);
        deviceSettings.add(sleepSettings);
        deviceSettings.add(notificationStatusSettings);

        return deviceSettings;
    }
    private void insertVitalDataToDB(List<HashMap<String, Object>> dataList, Map<String, String> deviceInfo) {
        if(!dataList.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();

            for (HashMap<String, Object> bloodPressureItem : dataList) {

                ContentValues cv = new ContentValues();

                calendar.setTimeInMillis((Long) bloodPressureItem.get(OmronConstants.OMRONVitalData.StartDateKey));

                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataCuffFlagKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.CuffFlagKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataDiastolicKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.DiastolicKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataIrregularFlagKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.IrregularFlagKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementDateKey, format.format(calendar.getTime()));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataMovementFlagKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.MovementFlagKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataPulseKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.PulseKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataSystolicKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.SystolicKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.StartDateKey)));

                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.AtrialFibrillationDetectionFlagKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementModeKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.MeasurementModeKey)));

                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataPositionIndicatorKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.PositioningIndicatorKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataConsecutiveMeasurementKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.ConsecutiveMeasurementKey)));
                cv.put(OmronDBConstans.VITAL_DATA_OMRONVitalDataIrregularHeartBeatCountKey, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.IrregularHeartBeatCountKey)));

                cv.put(OmronDBConstans.DEVICE_SELECTED_USER, String.valueOf(bloodPressureItem.get(OmronConstants.OMRONVitalData.UserIdKey)));
                cv.put(OmronDBConstans.DEVICE_LOCAL_NAME, deviceInfo.get(OmronConstants.OMRONDeviceInformation.LocalNameKey).toLowerCase());

                getContentResolver().insert(OmronDBConstans.VITAL_DATA_CONTENT_URI, cv);
            }
        }
    }
}