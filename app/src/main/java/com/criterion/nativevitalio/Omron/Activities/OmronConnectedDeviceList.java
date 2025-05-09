package com.criterion.nativevitalio.Omron.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.criterion.nativevitalio.Omron.adapter.ConnectedDeviceAdapter;
import com.criterion.nativevitalio.Omron.models.PairingDeviceData;
import com.criterion.nativevitalio.Omron.utility.Callback;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.Omron.utility.PreferencesManager;
import com.criterion.nativevitalio.Omron.utility.dataListData;
import com.criterion.nativevitalio.Omron.utility.sampleLog;
import com.criterion.nativevitalio.R;
import com.criterion.nativevitalio.UI.Home;
import com.criterion.nativevitalio.utils.MyApplication;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.SharedManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Omron HealthCare Inc
 */
public class OmronConnectedDeviceList extends BaseActivity {

    static final String TAG = "DeviceList";

    private ListView mListView;
    private ImageView mImageViewMenu,iv_back;
    private ImageView mImageViewInfo;
    private Button mAddButton;
    private Context mContext;
    private static List<Map<String, String>> fullDeviceList;
    private static List<Map<String, String>> soundDeviceList;
    private PreferencesManager preferencesManager = null;
    private PairingDeviceData pairingDeviceData;
    private ActivityResultLauncher<Intent> launcher;
    private  boolean isShowSupportList = false;
    private boolean isLoadSuccess = false;
    private String inputPartnerKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omron_connected_devices);

        mContext = this;
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(MyApplication.Companion.getAppContext());
        }
        initViews();
        initClickListeners();
        pairingDeviceData = new PairingDeviceData(getContentResolver());
        if (preferencesManager.getPartnerKey().isEmpty()) {
            showLibraryKeyDialog(true);
        } else {
            isLoadSuccess = loadDeviceList();
        }
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //List update process
                    refreshDeviceList();
                }
            }
        });

        if(Build.VERSION.SDK_INT < 31){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        100);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN
                                , Manifest.permission.BLUETOOTH_CONNECT
                        },
                        100);
            }
        }

    }
    // UI initializers

    private void initViews() {
        mImageViewInfo = findViewById(R.id.iv_info);
        mListView = findViewById(R.id.lv_devicelist);
        mImageViewMenu = findViewById(R.id.iv_menu);
        mAddButton = findViewById(R.id.btn_add);
        iv_back=findViewById(R.id.iv_back);
    }
    private void initClickListeners() {

        mImageViewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLibraryKeyDialog(false);
            }
        });


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), Home.class);
                 startActivity(intent);
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!personalData.isDataExists()){
                    showMessage("Info", "Please set personal setting",true, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(OmronConnectedDeviceList.this, UserPersonalSettingsActivity.class);
                            launcher.launch(intent);
                        }
                    });
                }else {
                    //Add New Device button
                    final Intent toScan = new Intent(OmronConnectedDeviceList.this, ScanActivity.class);
                    if(soundDeviceList != null && !soundDeviceList.isEmpty() && fullDeviceList != null && soundDeviceList.size() == fullDeviceList.size()) {
                        dataListData extraData = new dataListData(soundDeviceList);
                        toScan.putExtra(Constants.extraKeys.KEY_IS_BLE_PROTOCOL, 2);
                        toScan.putExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS, extraData);
                        launcher.launch(toScan);
                    }else if (soundDeviceList != null && !soundDeviceList.isEmpty()) {
                        showPopupMenu(new Callback<String>() {
                            @Override
                            public void onResult(String result) {
                                int selectedIndex = Integer.valueOf(result);
                                dataListData extraData;
                                if (selectedIndex == 1) {
                                    extraData = new dataListData(fullDeviceList);
                                }
                                else{
                                    extraData = new dataListData(soundDeviceList);
                                }
                                toScan.putExtra(Constants.extraKeys.KEY_IS_BLE_PROTOCOL, selectedIndex);
                                toScan.putExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS, extraData);
                                launcher.launch(toScan);
                            }
                        });
                    }else{
                        dataListData extraData = new dataListData(fullDeviceList);
                        toScan.putExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS, extraData);
                        launcher.launch(toScan);
                    }
                }
            }
        });

        mImageViewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, mImageViewMenu);
                popup.getMenuInflater().inflate(R.menu.menu_list, popup.getMenu());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    popup.setForceShowIcon(true);
                }else{
                    try {
                        Field[] fields = popup.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            if ("mPopup".equals(field.getName())) {
                                field.setAccessible(true);
                                Object menuPopupHelper = field.get(popup);
                                Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                        .getClass().getName());
                                Method setForceIcons = classPopupHelper.getMethod(
                                        "setForceShowIcon", boolean.class);
                                setForceIcons.invoke(menuPopupHelper, true);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                popup.show();
                final Intent[] _intent = new Intent[1];
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getOrder()){
                            case 1:
                                sampleLog.i(TAG,"1");
                                _intent[0] = new Intent(OmronConnectedDeviceList.this, UserPersonalSettingsActivity.class);
                                startActivity(_intent[0]);
                                break;
                            case 2:
                                sampleLog.i(TAG,"2");
                                _intent[0]  = new Intent(OmronConnectedDeviceList.this, DataDeviceListingActivity.class);
                                startActivity(_intent[0]);
                                break;
                            default:
                                sampleLog.i(TAG,"3");
                                _intent[0] = new Intent(OmronConnectedDeviceList.this, SupportDeviceListActivity.class);
                                dataListData extraData = new dataListData(fullDeviceList);
                                _intent[0].putExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS, extraData);
                                startActivity(_intent[0]);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    /**
     * Load devices
     */
    private boolean loadDeviceList() {
        fullDeviceList = new ArrayList<>();
        soundDeviceList = new ArrayList<>();
        SharedManager configManager = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext());
        Map<String, Object> retrieveManagerConfiguration = configManager.retrieveManagerConfiguration();
        if (retrieveManagerConfiguration != null) {
            addButtonEnable();
            fullDeviceList = (List<Map<String, String>>) retrieveManagerConfiguration.get(OmronConstants.OMRONBLEConfigDeviceKey);
            if(fullDeviceList != null && !fullDeviceList.isEmpty()) {
                for (Map<String, String> map : fullDeviceList) {//Does the support equipment have a sonic protocol?
                    if (map.containsKey("deviceProtocol") && map.get("deviceProtocol").equals("OMRONAudioProtocol")) {
                        soundDeviceList.add(map);
                    }
                }
                refreshDeviceList();
                if (isShowSupportList) {
                    isShowSupportList = false;
                    showSupportDeviceList();
                }
                return true;
            }
        }
        return false;
    }

    private void refreshDeviceList() {
        ConnectedDeviceAdapter mConnectedDeviceAdapter = new ConnectedDeviceAdapter(mContext, pairingDeviceData.getPairingDeviceList());
        mListView.setAdapter(mConnectedDeviceAdapter);
        mConnectedDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * Device selected and screen navigation
     *
     */
    public void showLibraryKeyDialog(boolean isInit) {

        Context ctx = MyApplication.Companion.getAppContext();
        String libraryVersion = OmronPeripheralManager.sharedManager(ctx).libVersion();
        PackageManager manager = OmronConnectedDeviceList.this.getPackageManager();
        try {

            PackageInfo info = manager.getPackageInfo(OmronConnectedDeviceList.this.getPackageName(), 0);
            String message = "\nSample App version : " + info.versionName + "\nLibrary Version : " + libraryVersion + "\n\nTap ⓘ to configure later\n";

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setCancelable(!isInit);
            alertDialogBuilder.setTitle("Configure OMRON Library Key");
            alertDialogBuilder.setMessage(message);

            // Set up the input
            final EditText inputField = new EditText(mContext);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            inputField.setInputType(InputType.TYPE_CLASS_TEXT);
            inputField.setHint("Enter API Key");
            alertDialogBuilder.setView(inputField);

            // Set up the buttons
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //isShowSupportList = true;
                    inputPartnerKey = inputField.getText().toString();
                    if (inputPartnerKey.isEmpty()) {
                        showErrorLoadingDevices();
                    } else {
                        // OmronConnectivityLibrary initialization and Api key setup.
                        OmronPeripheralManager.sharedManager(mContext).setAPIKey(inputPartnerKey, null);
                        addButtonDisable();
                        isShowSupportList = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mContext.registerReceiver(mMessageReceiver,
                                    new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification),RECEIVER_NOT_EXPORTED);
                        }else{
                            mContext.registerReceiver(mMessageReceiver,
                                    new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification));
                        }
                    }
                }
            });
            if (!isInit){
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if(!isLoadSuccess){
                            inputPartnerKey = preferencesManager.getPartnerKey();
                            OmronPeripheralManager.sharedManager(mContext).setAPIKey(inputPartnerKey, null);
                            addButtonDisable();
                            isShowSupportList = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                mContext.registerReceiver(mMessageReceiver,
                                        new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification),RECEIVER_NOT_EXPORTED);
                            }else{
                                mContext.registerReceiver(mMessageReceiver,
                                        new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification));
                            }
                        }
                    }
                });
            }
            alertDialogBuilder.show();

        } catch (PackageManager.NameNotFoundException e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mMessageReceiver != null) {
                mContext.unregisterReceiver(mMessageReceiver);
            }
            // Get extra data included in the Intent
            final int status = intent.getIntExtra(OmronConstants.OMRONConfigurationStatusKey, 0);

            if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileSuccess) {
                sampleLog.d(TAG, "Config File Extract Success");
                isLoadSuccess = loadDeviceList();
                if(isLoadSuccess) {
                    if (!inputPartnerKey.equals(preferencesManager.getPartnerKey())) {
                        preferencesManager.savePartnerKey(inputPartnerKey);
                        pairingDeviceData.deleteAllDevices();
                        refreshDeviceList();
                    }
                }else{
                    showErrorLoadingDevices();
                }
                addButtonEnable();
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileError) {
                sampleLog.d(TAG, "Config File Extract Failure");
                showErrorLoadingDevices();
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileUpdateError) {
                sampleLog.d(TAG, "Config File Update Failure");
                showErrorLoadingDevices();
            }
        }
    };
    private void showSupportDeviceList(){
        List<String> itemListForKey = new ArrayList<>();
        String key = OmronConstants.OMRONBLEConfigDevice.ModelDisplayName;
        String key2 = OmronConstants.OMRONBLEConfigDevice.Identifier;
        for (Map<String, String> map: fullDeviceList) {
            if (map.containsKey(key)) {
                String value = map.get(key) + "(" + map.get(key2) + ")";
                itemListForKey.add(value);
            }
        }

        // Create a custom layout
        View dialogLayout = getLayoutInflater().inflate(R.layout.popuo_listv_view, null);

        ListView listView = dialogLayout.findViewById(R.id.support_device_listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_text, itemListForKey);
        listView.setAdapter(adapter);

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setMessage("API Key registration successful!\n\nSupport Devices");
        // Create scrollview and add listview
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialogLayout.setLayoutParams(layoutParams);
        scrollView.addView(dialogLayout);
        builder.setView(scrollView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDeleteMenu(final Map<String, String> item){
        sampleLog.d(TAG,"showDeleteMenu  : " + item.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName));
        showMessageSelect("Delete device","Are you sure you want to delete this device?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        pairingDeviceData.deleteDevice(item);
                        refreshDeviceList();
                    }
                }
            );
    }
    public void selectDevice(Map<String, String> item){
        sampleLog.d(TAG,"selectDevice  : " + item.get(OmronConstants.OMRONBLEConfigDevice.ModelDisplayName));
        Intent toMain;
        switch(Integer.parseInt(Objects.requireNonNull(item.get(OmronConstants.OMRONBLEConfigDevice.Category)))){
            case OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE:
                toMain = new Intent(OmronConnectedDeviceList.this, BloodPressureMainActivity.class);
                break;
            case OmronConstants.OMRONBLEDeviceCategory.ACTIVITY:
                toMain = new Intent(OmronConnectedDeviceList.this, ActivityMainActivity.class);
                break;
            case OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION:
                toMain = new Intent(OmronConnectedDeviceList.this, WeightScaleMainActivity.class);
                break;
            case OmronConstants.OMRONBLEDeviceCategory.WHEEZE:
                toMain = new Intent(OmronConnectedDeviceList.this, WheezeMainActivity.class);
                break;
            case OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE:
                toMain = new Intent(OmronConnectedDeviceList.this, TemperatureRecordingActivity.class);
                break;
            case OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER:
                toMain = new Intent(OmronConnectedDeviceList.this, PulseOxymeterMainActivity.class);
                break;
            default:
                return;
        }
        toMain.putExtra(Constants.extraKeys.KEY_SELECTED_DEVICE, (HashMap<String, String>) item);
        launcher.launch(toMain);
    }
    public void showMessage(String title, String message,boolean cancelEnable,final DialogInterface.OnClickListener listene) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        TextView messageView = new TextView(mContext);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setGravity(Gravity.CENTER);
        int padding = (int) (16 * mContext.getResources().getDisplayMetrics().density); // 16dp padding
        messageView.setPadding(0, padding, 0, 0);
        alertDialogBuilder.setView(messageView);
        alertDialogBuilder.setPositiveButton("Ok",listene);
        alertDialogBuilder.setCancelable(cancelEnable);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public void showMessageSelect(String title, String message,final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setNegativeButton("CANCEL",null);
        alertDialogBuilder.setPositiveButton("OK",listener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void showErrorLoadingDevices() {
        showMessage("Info", "Invalid Library API Key configured",false, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLibraryKeyDialog(preferencesManager.getPartnerKey().isEmpty());
            }
        });
    }

    // Protocol selection menu
    private void showPopupMenu(final Callback<String> callback) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_user_select, null);
        final RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        final RadioButton radio1 = view.findViewById(R.id.radioButton1);
        final RadioButton radio2 = view.findViewById(R.id.radioButton2);
        radio1.setText("BLE device");
        radio2.setText("Sound wave device");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setView(view);
        alertDialogBuilder.setTitle("Protocol selection");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedButton = view.findViewById(selectedId);
                //alertDialog.dismiss();
                callback.onResult((String) selectedButton.getTag());
            }
        });
        alertDialogBuilder.setNegativeButton("CANCEL",null);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void addButtonEnable(){
        mAddButton.setEnabled(true);
    }
    private void addButtonDisable(){
        mAddButton.setEnabled(false);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN
            && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
