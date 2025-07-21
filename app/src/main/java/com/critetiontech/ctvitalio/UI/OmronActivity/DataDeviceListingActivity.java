package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.adapter.omronAdapter.DataSavedDevicesListAdapter;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.utils.PreferencesManager;
import com.critetiontech.ctvitalio.R;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Omron HealthCare Inc
 */
public class DataDeviceListingActivity extends BaseActivity {

    private PreferencesManager preferencesManager = null;
    private JSONArray deviceList;
    private Context mContext;
    RecyclerView rvSavedDevices;
    private DataSavedDevicesListAdapter dataSavedDevicesListAdapter;
    int category;
    String localName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_device_listing);
        mContext = this;
        preferencesManager = new PreferencesManager(DataDeviceListingActivity.this);
        deviceList = preferencesManager.getDataStoredDeviceList();
        rvSavedDevices = findViewById(R.id.rv_saved_devices);
        category = 0;
        localName = "";

        // HeartVue or Activity
        dataSavedDevicesListAdapter = new DataSavedDevicesListAdapter(preferencesManager.getDataStoredDeviceList(), new DataSavedDevicesListAdapter.DeviceItemSelect(){
            @Override
            public void onItemSelect(String localName, String identifier, Integer category, int position) {
                if (category == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) { // HeartVue or Activity
                    Intent toVitalData = new Intent(DataDeviceListingActivity.this, DataListingActivity.class);
                    toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                    toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, identifier);
                    startActivity(toVitalData);
                } else if (category == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER) {
                    Intent toVitalData = new Intent(DataDeviceListingActivity.this, PulseOxymeterDataListingActivity.class);
                    toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                    toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, identifier);
                    startActivity(toVitalData);
                } else if (category == OmronConstants.OMRONBLEDeviceCategory.WHEEZE) {
                    Intent toVitalData = new Intent(DataDeviceListingActivity.this, WheezeDataListingActivity.class);
                    toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                    toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, identifier);
                    startActivity(toVitalData);
                } else if (category == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {
                    Intent toVitalData = new Intent(DataDeviceListingActivity.this, WeightScaleDataListingActivity.class);
                    toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                    toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, identifier);
                    startActivity(toVitalData);
                } else {
                    Intent toVitalData = new Intent(DataDeviceListingActivity.this, BloodPressureDataListingActivity.class);
                    toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
                    toVitalData.putExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER, identifier);
                    startActivity(toVitalData);
                }
            }
        }, new DataSavedDevicesListAdapter.DeviceDelete() {
            @Override
            public void onItemDelete(View view, int position) {
                showConfirmationDialog(view, position);
            }
        });


        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(DataDeviceListingActivity.this, LinearLayoutManager.VERTICAL, false);
        rvSavedDevices.setLayoutManager(linearLayoutManager);
        rvSavedDevices.setAdapter(dataSavedDevicesListAdapter);

    }

    public void showConfirmationDialog(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete History")
                .setMessage("Are you sure you want to delete history for this device?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Processing when the CANCEL button is pressed
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Processing when OK button is pressed
                        // Execute the process to delete history
                        deleteItem(position);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem(int position) {
        try {
            category = dataSavedDevicesListAdapter.getItem(position).getInt(PreferencesManager.JSON_KEY_DEVICE_CATEGORY);
            localName = dataSavedDevicesListAdapter.getItem(position).getString(PreferencesManager.JSON_KEY_DEVICE_LOCAL_NAME);
            //Set deletion conditions
            Uri databaseURI = getDatabaseURI(category);
            String selection = OmronDBConstans.DEVICE_LOCAL_NAME + " COLLATE NOCASE=?";
            String[] selectionArgs = {localName};
            deleteDeviceHistory(databaseURI,selection, selectionArgs);
            deleteDevice(localName);
            refreshDeviceList(position);
        } catch (JSONException e) {
            e.printStackTrace();
            String errorMessage = "JSON ERROR: " + e.getMessage();
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    private void deleteDevice(String localName) {
        preferencesManager.removeDataFromStoredDeviceList(localName);
    }

    private Uri getDatabaseURI(int category) {
        Uri databaseURI;
        if (category == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            databaseURI = OmronDBConstans.ACTIVITY_DATA_CONTENT_URI;
        } else if (category == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER) {
            databaseURI = OmronDBConstans.OXYMETER_DATA_CONTENT_URI;
        } else if (category == OmronConstants.OMRONBLEDeviceCategory.WHEEZE) {
            databaseURI = OmronDBConstans.WHEEZE_DATA_CONTENT_URI;
        } else if (category == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {
            databaseURI = OmronDBConstans.WEIGHT_DATA_CONTENT_URI;
        } else {
            databaseURI = OmronDBConstans.VITAL_DATA_CONTENT_URI;
        }
        return databaseURI;
    }

    private void deleteDeviceHistory(Uri databaseURI, String selection, String[] selectionArgs) {
        // delete data
        mContext.getContentResolver().delete(databaseURI, selection, selectionArgs);
    }

    private void refreshDeviceList(int position) {
        dataSavedDevicesListAdapter.notifyItemRemoved(position);
        deviceList = preferencesManager.getDataStoredDeviceList();
        dataSavedDevicesListAdapter.updateItemList(deviceList);
    }
}
