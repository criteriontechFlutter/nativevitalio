package com.criterion.nativevitalio.Omron.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.criterion.nativevitalio.Omron.adapter.ScannedDevicesAdapter;
import com.criterion.nativevitalio.Omron.models.PairingDeviceData;
import com.criterion.nativevitalio.Omron.utility.Callback;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.Omron.utility.dataListData;
import com.criterion.nativevitalio.Omron.utility.sampleLog;
import com.criterion.nativevitalio.R;
import com.criterion.nativevitalio.utils.MyApplication;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectStateListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerStopScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.SharedManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main DashBoard activity, where perform the pairing and data transfer.
 */

public class ScanActivity extends BaseActivity {
    private Context mContext;
    private static final String TAG = "ScanActivity";

    private ListView mLvScannedList;
    private List<OmronPeripheral> mPeripheralList;
    private ScannedDevicesAdapter mScannedDevicesAdapter;
    private OmronPeripheral mSelectedPeripheral;
    private HashMap<String, String> SelectedDeviceInfo;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBar2;

    private List<Map<String, String>> deviceList;
    private boolean isBleDevice = false;

    private boolean isScan = false;
    private boolean isConnect = false;
    private static final String STR_DEVICE_INFO = "Device Information : ";

    private boolean isReceiverRegistered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mContext = this;

        Intent _intent = getIntent();
        if (_intent != null) {
            dataListData listData = _intent.getParcelableExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS);
            if (listData != null) {
                deviceList = listData.getDataList();
            }
            Integer protocol = _intent.getIntExtra(Constants.extraKeys.KEY_IS_BLE_PROTOCOL, 1);
            isBleDevice = protocol == 1;
        }

        initViews();
        initClickListeners();
        initLists();

        if (isBleDevice) {
            // Start OmronPeripheralManager
            startOmronPeripheralManager(false, true);
            // Set State Change Listener
            setStateChanges();
            startScan();
        } else {
            hideScanning();
        }
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
    private void startOmronPeripheralManager(boolean isHistoricDataRead, boolean isPairing) {

        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).getConfiguration();
        sampleLog.d(TAG, "Library Identifier : " + peripheralConfig.getLibraryIdentifier());

        // Filter device to scan and connect (optional)
        List<HashMap<String, String>> filterDevices = new ArrayList<>();
        for (Map<String, String> _device : deviceList) {
            if (_device != null && _device.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null && _device.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID) != null) {
                // Add item
                filterDevices.add((HashMap<String, String>) _device);
            }
        }
        peripheralConfig.deviceFilters = filterDevices;
        // Set Scan timeout interval (optional)
        peripheralConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT;
        // Set User Hash Id (mandatory)
        peripheralConfig.userHashId = "<email_address_of_user>"; // Set logged in user email

        ArrayList<HashMap> deviceSettings = new ArrayList<>();
        // Scan settings
        deviceSettings = (ArrayList<HashMap>) getScanSettings(deviceSettings);

        peripheralConfig.deviceSettings = deviceSettings;

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

    private void startScan() {
        // Start Scanning for Devices using OmronPeripheralManager
        isScan = true;
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).startScanPeripherals(new OmronPeripheralManagerScanListener() {

            @Override
            public void onScanCompleted(final List<OmronPeripheral> peripheralList, final OmronErrorInfo resultInfo) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultInfo.isSuccess()) {
                            boolean isAddItem = false;
                            for (OmronPeripheral peripheral : peripheralList) {
                                String name = peripheral.getModelName();
                                if (!name.isEmpty()) {
                                    isAddItem = true;
                                    String uuid = peripheral.getUuid();
                                    for (OmronPeripheral existingPeripheral : mPeripheralList) {
                                        if (existingPeripheral.getUuid().equals(uuid)) {
                                            isAddItem = false;
                                            break;
                                        }
                                    }
                                    if (isAddItem) {
                                        mPeripheralList.add(peripheral);
                                    }
                                }
                            }
                            if (mScannedDevicesAdapter != null && isAddItem) {
                                mScannedDevicesAdapter.setPeripheralList(mPeripheralList);
                                mScannedDevicesAdapter.notifyDataSetChanged();
                            }
                        } else {
                            isScan = false;
                            hideScanning();
                            showMessage("Info", resultInfo.getMessageInfo() + "\nError : " + resultInfo.getDetailInfo(), false, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent resultIntent = new Intent();
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void cancelScan() {
        if (isScan) {
            isScan = false;
            OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).stopScanPeripherals(new OmronPeripheralManagerStopScanListener() {
                @Override
                public void onStopScanCompleted(final OmronErrorInfo resultInfo) {
                }
            });
        }
    }

    private void disconnectDevice() {
        if (isConnect) {
            isConnect = false;
            // Disconnect device using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).disconnectPeripheral(mSelectedPeripheral, new OmronPeripheralManagerDisconnectListener() {
                @Override
                public void onDisconnectCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isConnect) {
            cancelScan();
            disconnectDevice();
            finish();
        }
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
                            isConnect = true;
                            status = "Connecting...";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.CONNECTED) {
                            status = "Connected";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTING) {
                            isConnect = false;
                            status = "Disconnecting...";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTED) {
                            status = "Disconnected";
                        }
                        sampleLog.d(TAG, status);
                    }
                });
            }
        });
    }

    void hideScanning() {
        mProgressBar.setVisibility(View.GONE);
    }

    void viewConnecting() {
        mProgressBar2.setVisibility(View.VISIBLE);
    }

    /**
     * Method to connect to the deviceIntent
     */
    private void connectPeripheral() {
        List<Integer> selectUsers = mSelectedPeripheral.getSelectedUsers();
        int users = Integer.parseInt(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Users));
        if (selectUsers.isEmpty() && users > 1) {
            showPopupMenu(users, new Callback<String>() {
                @Override
                public void onResult(String result) {
                    mSelectedPeripheral.setSelectedUsers(Collections.singletonList(Integer.valueOf(result)));
                    connectPeripheralNext();
                }
            });
        } else {
            connectPeripheralNext();
        }
    }

    private void connectPeripheralNext() {
        viewConnecting();
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              // Pair to Device using OmronPeripheralManager
                              OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).connectPeripheral(mSelectedPeripheral, new OmronPeripheralManagerConnectListener() {

                                          @Override
                                          public void onConnectCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
                                              connectionUpdateWithPeripheral(peripheral, resultInfo);
                                          }
                                      }
                              );
                          }
                      }
        );
    }

    //private RadioGroup radioGroup;
    private void showPopupMenu(int userCount, final Callback<String> callback) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_user_select, null);
        final RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton button3 = view.findViewById(R.id.radioButton3);
        RadioButton button4 = view.findViewById(R.id.radioButton4);
        if (userCount > 2) {
            button3.setVisibility(View.VISIBLE);
            button4.setVisibility(View.VISIBLE);
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setView(view);
        alertDialogBuilder.setTitle("Please select a user number");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedButton = view.findViewById(selectedId);
                //alertDialog.dismiss();
                callback.onResult((String) selectedButton.getTag());
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showConnectView() {
        Context _context = MyApplication.Companion.getAppContext();
        ImageView imageView = findViewById(R.id.imageView);
        TextView info1 = findViewById(R.id.device_info_text);
        TextView info2 = findViewById(R.id.device_info_text2);
        info1.setText("modelName : " + SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.ModelName));
        info2.setText("identifier : " + SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Identifier));
        int resourceId = _context.getResources().getIdentifier(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Image), "drawable", _context.getPackageName());
        imageView.setImageResource(resourceId);

        findViewById(R.id.rl_device_list).setVisibility(View.GONE);
        findViewById(R.id.ll_top).setVisibility(View.GONE);
        findViewById(R.id.rl_connecting_view).setVisibility(View.VISIBLE);
    }

    private void connectionUpdateWithPeripheral(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
        viewConnecting();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (resultInfo.isSuccess() && peripheral != null) {
                    PairingDeviceData.addPairingDataToDB(mSelectedPeripheral, null);
                    showMessage("Device paired", "Omron device paired successfully!", false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelScan();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    });
                } else {
                    sampleLog.i(TAG, resultInfo.getDetailInfo());
                    sampleLog.i(TAG, resultInfo.getMessageInfo());
                    showMessage("Info", resultInfo.getMessageInfo() + "\nError : " + resultInfo.getDetailInfo(), false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    });
                }
            }
        });
    }
    /*******************************************************************************************/
    /************************ Section for Activity Device / HeartVue **************************/
    /*******************************************************************************************/
    private List<HashMap> getBloodPressureSettings(List<HashMap> deviceSettings) {

        // Blood Pressure
        if (Integer.parseInt(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
            HashMap<String, Object> bloodPressurePersonalSettings = new HashMap<>();
            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.Off);
            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval60);
            HashMap<String, Object> settings = new HashMap<>();
            settings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey, bloodPressurePersonalSettings);
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey, personalData.getBirthdayNum());
            HashMap<String, HashMap> _personalSettings = new HashMap<>();
            _personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);

            // Personal settings for device
            deviceSettings.add(_personalSettings);
        }

        return deviceSettings;
    }

    private List<HashMap> getBCMSettings(List<HashMap> deviceSettings) {

        // body composition
        if (Integer.parseInt(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {

            HashMap<String, Object> settings = new HashMap<>();
            if (Integer.parseInt(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Users)) > 1) {
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

            deviceSettings.add(personalSettings);
            deviceSettings.add(weightSettings);
        }

        return deviceSettings;
    }

    private List<HashMap> getActivitySettings(List<HashMap> deviceSettings) {

        // Activity Tracker
        if (Integer.parseInt(SelectedDeviceInfo.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY
            // Set Personal Settings in Configuration (mandatory for Activity devices)
        ) {

            HashMap<String, String> settingsModel = new HashMap<String, String>();
            settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, personalData.getHeight());
            settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserWeightKey, personalData.getWeight());
            settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserStrideKey, personalData.getStride());

            HashMap<String, HashMap> userSettings = new HashMap<>();
            userSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settingsModel);

            // Notification settings
            ArrayList<String> notificationsAvailable = new ArrayList<>();
            notificationsAvailable.add("android.intent.action.PHONE_STATE");
            notificationsAvailable.add("com.google.android.gm");
            notificationsAvailable.add("android.provider.Telephony.SMS_RECEIVED");
            notificationsAvailable.add(" com.criterion.nativevitalio");
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
        }

        return deviceSettings;
    }

    private List<HashMap> getScanSettings(List<HashMap> deviceSettings) {

        // Scan Settings
        HashMap<String, Object> ScanModeSettings = new HashMap<>();
        HashMap<String, HashMap> ScanSettings = new HashMap<>();
        ScanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.Pairing);
        ScanSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, ScanModeSettings);

        deviceSettings.add(ScanSettings);

        return deviceSettings;
    }

    private void initClickListeners() {
        // To perform scan process.
        findViewById(R.id.btn_Cancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void initViews() {
        mLvScannedList = findViewById(R.id.lv_scannedlist);
        mProgressBar = findViewById(R.id.pb_scan);
        mProgressBar2 = findViewById(R.id.pb_scan2);
        if (!isBleDevice) {
            TextView textView = findViewById(R.id.tv_device_list);
            textView.setText("Select sound wave device");
        }
    }

    private void initLists() {
        mPeripheralList = new ArrayList<>();
        if (!isBleDevice) {
            for (Map<String, String> map : deviceList) {
                String identifierString = map.get(OmronConstants.OMRONBLEConfigDevice.Identifier);
                mPeripheralList.add(new OmronPeripheral(identifierString, null));
            }
        }
        mScannedDevicesAdapter = new ScannedDevicesAdapter(mContext, mPeripheralList);
        mLvScannedList.setAdapter(mScannedDevicesAdapter);
        mLvScannedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedPeripheral = mScannedDevicesAdapter.getItem(position);
                if (PairingDeviceData.isDeviceAvailable(mSelectedPeripheral)) {
                    //Device is already registered
                    showMessage("Info", "This device is already registered.",true , null);
                } else {
                    if (isBleDevice) {
                        Context _context = MyApplication.Companion.getAppContext();
                        SharedManager configManager = OmronPeripheralManager.sharedManager(_context);
                        SelectedDeviceInfo = (HashMap<String, String>) configManager.getConfiguration().getDeviceConfigGroupIdAndGroupIncludedId(mSelectedPeripheral.getDeviceGroupIDKey(), mSelectedPeripheral.getDeviceGroupIncludedGroupIDKey());
                        connectStart();
                    } else {
                        PairingDeviceData.addPairingDataToDB(mSelectedPeripheral, null);
                        showMessage("Device paired", "Omron device paired successfully!",false, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        });
                    }
                }
            }
        });
    }

    private void connectStart() {
        hideScanning();
        cancelScan();
        showConnectView();
        initLists();
        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).getConfiguration();
        ArrayList<HashMap> deviceSettings = (ArrayList<HashMap>) peripheralConfig.deviceSettings;
        // Blood pressure settings (optional)
        deviceSettings = (ArrayList<HashMap>) getBloodPressureSettings(deviceSettings);

        // Blood pressure settings (optional)
        deviceSettings = (ArrayList<HashMap>) getBCMSettings(deviceSettings);

        // Activity device settings (optional)
        deviceSettings = (ArrayList<HashMap>) getActivitySettings(deviceSettings);

        peripheralConfig.deviceSettings = deviceSettings;
        OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext()).setConfiguration(peripheralConfig);
        connectPeripheral();
    }


    private void resetDeviceList() {

        if (mScannedDevicesAdapter != null) {
            hideScanning();
            mPeripheralList = new ArrayList<>();
            mScannedDevicesAdapter.setPeripheralList(mPeripheralList);
            mScannedDevicesAdapter.notifyDataSetChanged();
        }
    }

    public void showMessage(String title, String message, boolean isCancelEnable,final DialogInterface.OnClickListener listene) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        TextView messageView = new TextView(mContext);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setGravity(Gravity.CENTER);
        int padding = (int) (16 * mContext.getResources().getDisplayMetrics().density); // 16dp padding
        messageView.setPadding(0, padding, 0, 0);
        alertDialogBuilder.setView(messageView);
        alertDialogBuilder.setCancelable(isCancelEnable);
        alertDialogBuilder.setPositiveButton("Ok", listene);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}