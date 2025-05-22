package com.critetiontech.ctvitalio.UI.OmronActivity;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.model.PairingDeviceData;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.utils.PreferencesManager;
import com.critetiontech.ctvitalio.utils.sampleLog;
import com.critetiontech.ctvitalio.R;
import com.critetiontech.ctvitalio.utils.MyApplication;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PulseOxymeterMainActivity extends BaseActivity {

    private Context mContext;
    private static final String TAG = "OmronSampleApp";
    private OmronPeripheral mSelectedPeripheral;
    private List<Integer> selectedUsers = new ArrayList<>();
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
                            Toast.makeText(PulseOxymeterMainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
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
        transferUsersDataWithPeripheral(true);
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
                                    vitalDataList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataPulseOximeterKey);
                                    preferencesManager.addDataStoredDeviceList(device.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName(), device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
                                    insertVitalDataToDB(vitalDataList, deviceInfo);
                                }
                                showVitalDataResult(vitalDataList);
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
    //individual setting
    private TextView mTvTImeStamp,mTvSpO2,mTvPulseRate;

    private void initViews() {
        initCommonViews();
        Resources res = mContext.getResources();
        //Title bar Text
        TextView textView;
        textView = findViewById(R.id.tvLabel);
        textView.setText(device.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName) + " - Connection Status");
        textView = findViewById(R.id.device_info);
        textView.setText(res.getString(R.string.oxygen_saturation));
        RelativeLayout layout = findViewById(R.id.rl_device_information_view);
        layout.setVisibility(View.GONE);

        LinearLayout parentLayout2 = findViewById(R.id.linear_layout2);
        mTvTImeStamp = addLayoutToScrollView(parentLayout2,res.getString(R.string.time_stamp));
        mTvSpO2 = addLayoutToScrollView(parentLayout2,res.getString(R.string.spo2));
        mTvPulseRate = addLayoutToScrollView(parentLayout2,res.getString(R.string.pulse_rate));
        LinearLayout parentLayout3 = findViewById(R.id.linear_layout3);
        parentLayout3.setVisibility(View.GONE);
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
                Intent toVitalData = new Intent(PulseOxymeterMainActivity.this, PulseOxymeterDataListingActivity.class);
                toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, mSelectedPeripheral.getLocalName());
                toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, device.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
                startActivity(toVitalData);
            }
        });
    }
    private void showVitalDataResult(final List<HashMap<String, Object>> vitalData) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                resetVitalDataResult();

                if (vitalData.size() == 0) {

                    mTvErrorDesc.setText("No New readings transferred");

                } else {
                    HashMap<String, Object> vitalDataItem = vitalData.get(vitalData.size() - 1);

                    mTvErrorDesc.setText("-");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((Long) vitalDataItem.get(OmronConstants.OMRONPulseOximeterData.StartDateKey));
                    mTvTImeStamp.setText(getDateTime(calendar.getTime()));
                    setTextView(mTvSpO2,vitalDataItem,OmronConstants.OMRONPulseOximeterData.SPO2LevelKey,"\t %");
                    setTextView(mTvPulseRate,vitalDataItem,OmronConstants.OMRONPulseOximeterData.PulseRateKey,"\t bpm");
                    PairingDeviceData.addPairingDataToDB(mSelectedPeripheral,null);
                }
            }
        });

    }
    private void resetVitalDataResult() {
        mTvTImeStamp.setText("-");
        mTvSpO2.setText("-");
        mTvPulseRate.setText("-");
    }
    //individual setting
    private void insertVitalDataToDB(List<HashMap<String, Object>> dataList, Map<String, String> deviceInfo) {
        for (HashMap<String, Object> weightItem : dataList) {

            sampleLog.d("Weight - ", weightItem.toString());

        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();

        for (HashMap<String, Object> pulseOximeterItem : dataList) {

            ContentValues cv = new ContentValues();

            calendar.setTimeInMillis((Long) pulseOximeterItem.get(OmronConstants.OMRONPulseOximeterData.StartDateKey));

            cv.put(OmronDBConstans.DEVICE_SELECTED_USER, String.valueOf(pulseOximeterItem.get(OmronConstants.OMRONPulseOximeterData.UserIdKey)));
            cv.put(OmronDBConstans.OXYMETER_DATA_StartTimeKey, format.format(calendar.getTime()));
            cv.put(OmronDBConstans.OXYMETER_DATA_SpO2Key, String.valueOf(pulseOximeterItem.get(OmronConstants.OMRONPulseOximeterData.SPO2LevelKey)));
            cv.put(OmronDBConstans.OXYMETER_DATA_PulseKey, String.valueOf(pulseOximeterItem.get(OmronConstants.OMRONPulseOximeterData.PulseRateKey)));
            cv.put(OmronDBConstans.DEVICE_LOCAL_NAME, deviceInfo.get(OmronConstants.OMRONDeviceInformation.LocalNameKey).toLowerCase());

            getContentResolver().insert(OmronDBConstans.OXYMETER_DATA_CONTENT_URI, cv);
        }
    }
}
