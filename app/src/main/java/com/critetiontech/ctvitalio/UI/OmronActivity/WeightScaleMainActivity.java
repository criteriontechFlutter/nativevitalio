package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import java.math.BigDecimal;
public class WeightScaleMainActivity extends BaseActivity {

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
            preferencesManager = new PreferencesManager(WeightScaleMainActivity.this);
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
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            mContext.unregisterReceiver(mMessageReceiver);
        }
    }
    /**
     * Configure library functionalities
     */
    private void startOmronPeripheralManager(boolean isHistoricDataRead) {

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
        // Disclaimer: Read definition before usage
        peripheralConfig.enableAllDataRead = isHistoricDataRead;
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
                            Toast.makeText(WeightScaleMainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Transfer");
        alertDialogBuilder.setMessage("Do you want to transfer all historic readings from device?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        transferUsersDataWithPeripheral(true);
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        transferUsersDataWithPeripheral(false);
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Data transfer with multiple users
    private void transferUsersDataWithPeripheral(final boolean isHistoricDataRead) {
        startOmronPeripheralManager(isHistoricDataRead);
        // Set State Change Listener
        setStateChanges();
        connectStatus = connectStatus_Scanning;
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).startDataTransferFromPeripheral(mSelectedPeripheral, selectedUsers, true, new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
                if (resultInfo.isSuccess() && peripheral != null) {
                    mSelectedPeripheral = peripheral; // Saving for Transfer Function
                    OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).endDataTransferFromPeripheral(new OmronPeripheralManagerDataTransferListener() {
                        @Override
                        public void onDataTransferCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo2) {
                            if (resultInfo2.isSuccess() && peripheral != null) {
                                ArrayList<HashMap<String, Object>> vitalDataList = null;
                                HashMap<String, Object> vitalData = (HashMap<String, Object>) peripheral.getVitalData();
                                HashMap<String, String> deviceInfo = (HashMap<String, String>) peripheral.getDeviceInformation();
                                if (vitalData != null) {
                                    // VitalData Data
                                    vitalDataList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);
                                    preferencesManager.addDataStoredDeviceList(device.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName(), device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
                                    insertVitalDataToDB(vitalDataList, deviceInfo);
                                }
                                // Setting Data
                                ArrayList<Map> allSettings = new ArrayList<Map>(peripheral.getDeviceSettings());
                                Map<String, Object> personalSettingsItem = null;
                                Object objectItem = peripheral.getDeviceSettingsWithUser(selectedUsers.get(0));
                                if (!(objectItem instanceof OmronErrorInfo)) {
                                    personalSettingsItem = (Map<String, Object>) objectItem;
                                }
                                showVitalDataResult(vitalDataList, allSettings, personalSettingsItem, deviceInfo);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setStatus("-");
                                        mTvErrorCode.setText(resultInfo2.getDetailInfo());
                                        mTvErrorDesc.setText(resultInfo2.getMessageInfo());
                                        enableDisableButton(true);
                                    }
                                });
                            }
                        }
                    });
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
    private TextView addLayoutToScrollView(LinearLayout parentLayout ,String tagText) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layoutView = inflater.inflate(R.layout.list_item_display, parentLayout, false);

        TextView tagTextView = layoutView.findViewById(R.id.tagText);
        tagTextView.setText(tagText);

        TextView setValueTextView = layoutView.findViewById(R.id.setValue);

        parentLayout.addView(layoutView);
        return setValueTextView;
    }
    private void setTextView(TextView view, Map<String, Object> dataList, String key){
        Object objectData = dataList.get(key);
        if (objectData != null) {
            view.setText(String.valueOf(objectData));
        }
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
    private TextView mTvTImeStamp, mWeightData1, mWeightData2, mWeightData3, mTvUserSelected, mWeightData4, mBmi, mBodyAge , mVisceralFat, mTvSeqNum;
    private TextView mTvDateOfBirth, mTvGender, mTvHeight,mTvBatteryRem;
    private TextView mTvTimeFormat, mTvDateFormat, mTvWeightUnit, mTvDpBodyFat, mTvDpVisceralFat;
    private TextView mTvDpSkeletalMascle,mTvDpRestingMetab,mTvDpBMI,mTvDpBodyAge,mTvDeBodyFat;
    private TextView mTvDeVisceralFat,mTvDeSkeletalMascle,mTvDeRestingMetab,mTvDeBMI,mTvDeBodyAge;
    private boolean isDevelopmentTest = false;
    private void initViews() {
        initCommonViews();
        if (BuildConfig.BUILD_TYPE.equals("devtest")) {
            //Enable display only during Library development testing
            isDevelopmentTest = true;
        }
        //Titlebar TEXT
        TextView textView;
        textView = findViewById(R.id.tvLabel);
        textView.setText(device.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName) + " - Connection Status");
        textView = findViewById(R.id.device_info);
        textView.setText("Weight");

        Resources res = mContext.getResources();
        LinearLayout parentLayout2 = findViewById(R.id.linear_layout2);
        mTvTImeStamp = addLayoutToScrollView(parentLayout2,res.getString(R.string.time_stamp));
        mWeightData1 = addLayoutToScrollView(parentLayout2,res.getString(R.string.user_weight));
        mWeightData2 = addLayoutToScrollView(parentLayout2,res.getString(R.string.user_body_fat));
        mWeightData3 = addLayoutToScrollView(parentLayout2,res.getString(R.string.resting_metabolism));
        mWeightData4 = addLayoutToScrollView(parentLayout2,res.getString(R.string.skeletal_muscle));
        mBmi = addLayoutToScrollView(parentLayout2,res.getString(R.string.bmi));
        mBodyAge = addLayoutToScrollView(parentLayout2,res.getString(R.string.user_body_age));
        mVisceralFat = addLayoutToScrollView(parentLayout2,res.getString(R.string.visceral_fat));
        mTvUserSelected = addLayoutToScrollView(parentLayout2,res.getString(R.string.user_selected));
        if(isDevelopmentTest) {
            mTvSeqNum = addLayoutToScrollView(parentLayout2, res.getString(R.string.sequence_number));
        }

        LinearLayout parentLayout3 = findViewById(R.id.linear_layout3);
        mTvDateOfBirth = addLayoutToScrollView(parentLayout3,res.getString(R.string.date_of_birth));
        mTvGender = addLayoutToScrollView(parentLayout3,res.getString(R.string.user_gender));
        mTvHeight = addLayoutToScrollView(parentLayout3,res.getString(R.string.height));
        mTvBatteryRem = addLayoutToScrollView(parentLayout3,res.getString(R.string.battery_rem));
        if(isDevelopmentTest){
            mTvTimeFormat = addLayoutToScrollView(parentLayout3,res.getString(R.string.time_format_setting));
            mTvDateFormat = addLayoutToScrollView(parentLayout3,res.getString(R.string.date_format_setting));
            mTvWeightUnit = addLayoutToScrollView(parentLayout3,res.getString(R.string.weight_Unit));
            mTvDpBodyFat = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_body_fat));
            mTvDpVisceralFat = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_visceral_fat_lv));
            mTvDpSkeletalMascle = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_skeletal_muscle_lv));
            mTvDpRestingMetab = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_resting_metabolism));
            mTvDpBMI = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_bmi));
            mTvDpBodyAge = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_pri_body_age));
            mTvDeBodyFat = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_body_fat));
            mTvDeVisceralFat = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_visceral_fat_lv));
            mTvDeSkeletalMascle = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_skeletal_muscle_lv));
            mTvDeRestingMetab = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_resting_metabolism));
            mTvDeBMI = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_bmi));
            mTvDeBodyAge = addLayoutToScrollView(parentLayout3,res.getString(R.string.display_ena_body_age));
            mTvSeqNum.setText(mSequenceNoString != null ? mSequenceNoString : "-");
        }
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
                Intent toVitalData = new Intent(WeightScaleMainActivity.this, WeightScaleDataListingActivity.class);
                toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, mSelectedPeripheral.getLocalName());
                toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
                startActivity(toVitalData);
            }
        });
    }
    private void showVitalDataResult(final List<HashMap<String, Object>> weightData, final List<Map> allSettingItems, final Map<String, Object> personalSettingsItem, final Map<String, String> deviceInfo) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                resetVitalDataResult();

                if (weightData.size() == 0) {

                    mTvErrorDesc.setText("No New readings transferred");

                } else {

                    HashMap<String, Object> weightDataItem = weightData.get(weightData.size() - 1);

                    mTvErrorDesc.setText("-");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((Long) weightDataItem.get(OmronConstants.OMRONWeightData.StartDateKey));
                    mTvTImeStamp.setText(getDateTime(calendar.getTime()));

                    String weightText = "-";
                    if(weightDataItem.get(OmronConstants.OMRONWeightData.WeightKey) != null) {
                        weightText = weightDataItem.get(OmronConstants.OMRONWeightData.WeightKey) + " Kg";
                        if(weightDataItem.containsKey(OmronConstants.OMRONWeightData.WeightLbsKey)){
                            weightText = weightText + " / " + weightDataItem.get(OmronConstants.OMRONWeightData.WeightLbsKey) + " Lbs";
                        }
                    }
                    mWeightData1.setText(weightText);

                    Object objectData;
                    objectData = weightDataItem.get(OmronConstants.OMRONWeightData.UserIdKey);
                    if (objectData != null) {
                        mTvUserSelected.setText("User " + objectData);
                    }
                    objectData = weightDataItem.get(OmronConstants.OMRONWeightData.SequenceKey);
                    if(objectData != null){
                        mSequenceNoString= String.valueOf(objectData);
                        if(isDevelopmentTest) {
                            mTvSeqNum.setText(mSequenceNoString);
                        }
                    }
                    PairingDeviceData.addPairingDataToDB(mSelectedPeripheral,mSequenceNoString);
                    setTextView(mBodyAge,weightDataItem,OmronConstants.OMRONWeightData.BodyAgeKey);

                    setTextView(mWeightData2,weightDataItem,OmronConstants.OMRONWeightData.BodyFatPercentageKey);

                    setTextView(mVisceralFat,weightDataItem,OmronConstants.OMRONWeightData.VisceralFatLevelKey);

                    setTextView(mWeightData3,weightDataItem,OmronConstants.OMRONWeightData.RestingMetabolismKey);

                    setTextView(mWeightData4,weightDataItem,OmronConstants.OMRONWeightData.SkeletalMusclePercentageKey);

                    setTextView(mBmi,weightDataItem,OmronConstants.OMRONWeightData.BMIKey);

                }
                if(personalSettingsItem != null){

                    String stringDateOfBirth = personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey).toString();
                    Date date = getDateOfBirthDateType(stringDateOfBirth,"yyyyMMdd");
                    stringDateOfBirth = getDate(date);
                    mTvDateOfBirth.setText(stringDateOfBirth);

                    if(personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey) != null) {
                        int genderSetting = (int)personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey);
                        if (genderSetting == OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female) {
                            mTvGender.setText(genderSetting + "(Female)");
                        } else if (genderSetting == OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male) {
                            mTvGender.setText(genderSetting + "(Male)");
                        }
                    }

                    String userHeight = (String) personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey);
                    if (userHeight != null) {
                        int userHeightExp = (int) personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.UserHeightExponentKey);
                        double userHeightValue = Double.parseDouble(userHeight) * Math.pow(10, userHeightExp);
                        int exp = userHeightExp > 0 ? 0 : userHeightExp * -1;
                        mTvHeight.setText(new BigDecimal(String.format("%." + exp + "f", userHeightValue)).stripTrailingZeros().toPlainString());
                    }

                    if(isDevelopmentTest) {
                        if (personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.WeightKey) != null) {
                            final HashMap<String, Object> personalWeightItem = (HashMap<String, Object>) personalSettingsItem.get(OmronConstants.OMRONDevicePersonalSettings.WeightKey);
                            setTextView(mTvDpBodyFat,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPriorityBodyFatKey);
                            setTextView(mTvDpVisceralFat,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPriorityVisceralFatLevelKey);
                            setTextView(mTvDpSkeletalMascle,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPrioritySkeletalMuscleLevelKey);
                            setTextView(mTvDpRestingMetab,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPriorityRestingMetabolismKey);
                            setTextView(mTvDpBMI,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPriorityBMIKey);
                            setTextView(mTvDpBodyAge,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayPriorityBodyAgeKey);
                            setTextView(mTvDeBodyFat,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableBodyFatKey);
                            setTextView(mTvDeVisceralFat,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableVisceralFatLevelKey);
                            setTextView(mTvDeSkeletalMascle,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableSkeletalMuscleLevelKey);
                            setTextView(mTvDeRestingMetab,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableRestingMetabolismKey);
                            setTextView(mTvDeBMI,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableBMIKey);
                            setTextView(mTvDeBodyAge,personalWeightItem,OmronConstants.OMRONDevicePersonalSettings.WeightDisplayEnableBodyAgeKey);
                        }
                    }
                }
                Map<String,Object> _info = new HashMap<String, Object>(deviceInfo);
                setTextView(mTvBatteryRem,_info,OmronConstants.OMRONDeviceInformation.BatteryRemainingKey,"%");
                if(isDevelopmentTest) {
                    Map<String, Object> timeSettingsItem = null;
                    Map<String, Object> dateSettingsItem = null;
                    Map<String, Object> weightSettingsItem = null;
                    if(allSettingItems != null) {
                        for (Map<String, Object> item : allSettingItems) {
                            if (item.containsKey(OmronConstants.OMRONDeviceTimeSettingsKey)) {
                                timeSettingsItem = (HashMap<String, Object>) item.get(OmronConstants.OMRONDeviceTimeSettingsKey);
                            } else if (item.containsKey(OmronConstants.OMRONDeviceDateSettingsKey)) {
                                dateSettingsItem = (HashMap<String, Object>) item.get(OmronConstants.OMRONDeviceDateSettingsKey);
                            } else if (item.containsKey(OmronConstants.OMRONDeviceWeightSettingsKey)) {
                                weightSettingsItem = (HashMap<String, Object>) item.get(OmronConstants.OMRONDeviceWeightSettingsKey);
                            }
                        }
                    }
                    if (timeSettingsItem != null
                            && timeSettingsItem.get(OmronConstants.OMRONDeviceTimeSettings.FormatKey) != null) {
                        int timeFormat = (int) timeSettingsItem.get(OmronConstants.OMRONDeviceTimeSettings.FormatKey);
                        if (timeFormat == OmronConstants.OMRONDeviceTimeFormat.Time24Hour) {
                            mTvTimeFormat.setText(timeFormat + "(24h)");
                        } else if (timeFormat == OmronConstants.OMRONDeviceTimeFormat.Time12Hour) {
                            mTvTimeFormat.setText(timeFormat + "(12h)");
                        }
                    }

                    if (dateSettingsItem != null
                            && dateSettingsItem.get(OmronConstants.OMRONDeviceDateSettings.FormatKey) != null) {
                        int dateFormat = (int) dateSettingsItem.get(OmronConstants.OMRONDeviceDateSettings.FormatKey);
                        if (dateFormat == OmronConstants.OMRONDeviceDateFormat.MonthDay) {
                            mTvDateFormat.setText(dateFormat + "(MM/DD)");
                        } else if (dateFormat == OmronConstants.OMRONDeviceDateFormat.DayMonth) {
                            mTvDateFormat.setText(dateFormat + "(DD/MM)");
                        }
                    }

                    if (weightSettingsItem != null
                            && weightSettingsItem.get(OmronConstants.OMRONDeviceWeightSettings.UnitKey) != null) {
                        int weightUnit = (int) weightSettingsItem.get(OmronConstants.OMRONDeviceWeightSettings.UnitKey);
                        switch (weightUnit) {
                            case OmronConstants.OMRONDeviceWeightUnit.Kg:
                                mTvWeightUnit.setText(weightUnit + "(kg)");
                                break;
                            case OmronConstants.OMRONDeviceWeightUnit.St:
                                mTvWeightUnit.setText(weightUnit + "(St)");
                                break;
                            case OmronConstants.OMRONDeviceWeightUnit.Lbs:
                                mTvWeightUnit.setText(weightUnit + "(lbs)");
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });

    }
    private void resetVitalDataResult() {
        mTvTImeStamp.setText("-");
        mWeightData1.setText("-");
        mWeightData2.setText("-");
        mWeightData3.setText("-");
        mWeightData4.setText("-");
        mBmi.setText("-");
        mBodyAge.setText("-");
        mVisceralFat.setText("-");
        mTvDateOfBirth.setText("-");
        mTvGender.setText("-");
        mTvHeight.setText("-");
        mTvBatteryRem.setText("-");
        if(isDevelopmentTest){
            mTvTimeFormat.setText("-");
            mTvDateFormat.setText("-");
            mTvWeightUnit.setText("-");
            mTvDpBodyFat.setText("-");
            mTvDpVisceralFat.setText("-");
            mTvDpSkeletalMascle.setText("-");
            mTvDpRestingMetab.setText("-");
            mTvDpBMI.setText("-");
            mTvDpBodyAge.setText("-");
            mTvDeBodyFat.setText("-");
            mTvDeVisceralFat.setText("-");
            mTvDeSkeletalMascle.setText("-");
            mTvDeRestingMetab.setText("-");
            mTvDeBMI.setText("-");
            mTvDeBodyAge.setText("-");
            mTvSeqNum.setText("-");
        }
        mTvUserSelected.setText("-");
    }
    //Individual settings
    private List<HashMap> getPersonalSettings(List<HashMap> deviceSettings) {
        HashMap<String, Object> settings = new HashMap<>();
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Users)) > 1) {
            // BCM configuration
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, String.format("%.0f",Double.parseDouble(personalData.getHeight()) * 100));
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey, personalData.getGenderValue());
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey, personalData.getBirthdayNum());
        }

        HashMap<String, HashMap> personalSettings = new HashMap<>();
        personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);


        // Weight Settings
        // Add other weight common settings if any
        HashMap<String, Object> weightCommonSettings = new HashMap<>();
        weightCommonSettings.put(OmronConstants.OMRONDeviceWeightSettings.UnitKey, personalData.getUnitValue());
        HashMap<String, Object> weightSettings = new HashMap<>();
        weightSettings.put(OmronConstants.OMRONDeviceWeightSettingsKey, weightCommonSettings);

        // Time Format
        HashMap<String, Object> timeFormatSettings = new HashMap<>();
        timeFormatSettings.put(OmronConstants.OMRONDeviceTimeSettings.FormatKey, OmronConstants.OMRONDeviceTimeFormat.Time24Hour);
        HashMap<String, HashMap> timeSettings = new HashMap<>();
        timeSettings.put(OmronConstants.OMRONDeviceTimeSettingsKey, timeFormatSettings);

        deviceSettings.add(personalSettings);
        deviceSettings.add(weightSettings);
        deviceSettings.add(timeSettings);
        return deviceSettings;
    }
    private void insertVitalDataToDB(List<HashMap<String, Object>> dataList, Map<String, String> deviceInfo) {
        if (!dataList.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();

            for (HashMap<String, Object> weightItem : dataList) {

                ContentValues cv = new ContentValues();

                calendar.setTimeInMillis((Long) weightItem.get(OmronConstants.OMRONWeightData.StartDateKey));
                cv.put(OmronDBConstans.DEVICE_SELECTED_USER, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.UserIdKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_StartTimeKey, format.format(calendar.getTime()));
                cv.put(OmronDBConstans.WEIGHT_DATA_WeightKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.WeightKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_BodyFatLevelKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.BodyFatLevelClassificationKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_BodyFatPercentageKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.BodyFatPercentageKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_RestingMetabolismKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.RestingMetabolismKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_SkeletalMuscleKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.SkeletalMusclePercentageKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_BMIKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.BMIKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_BodyAgeKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.BodyAgeKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_VisceralFatLevelKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.VisceralFatLevelKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_VisceralFatLevelClassificationKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.VisceralFatLevelClassificationKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_SkeletalMuscleLevelKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.SkeletalMuscleLevelClassificationKey)));
                cv.put(OmronDBConstans.WEIGHT_DATA_BMIClassificationKey, String.valueOf(weightItem.get(OmronConstants.OMRONWeightData.BMILevelClassificationKey)));
                cv.put(OmronDBConstans.DEVICE_LOCAL_NAME, deviceInfo.get(OmronConstants.OMRONDeviceInformation.LocalNameKey).toLowerCase());

                getContentResolver().insert(OmronDBConstans.WEIGHT_DATA_CONTENT_URI, cv);
            }
        }
    }
}
