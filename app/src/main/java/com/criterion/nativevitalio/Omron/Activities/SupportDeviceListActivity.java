package com.criterion.nativevitalio.Omron.Activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;


import com.criterion.nativevitalio.Omron.adapter.SupportDeviceAdapter;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.Omron.utility.dataListData;
import com.criterion.nativevitalio.R;

import java.util.List;
import java.util.Map;

public class SupportDeviceListActivity extends AppCompatActivity {
    private List<Map<String, String>> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_device_list);
        dataListData listData = getIntent().getParcelableExtra(Constants.extraKeys.KEY_DEVICE_INFO_LISTS);
        deviceList = listData.getDataList();
        SupportDeviceAdapter supportDeviceAdapter = new SupportDeviceAdapter(this, deviceList);
        ListView listView = findViewById(R.id.lv_devicelist);
        listView.setAdapter(supportDeviceAdapter);
        supportDeviceAdapter.notifyDataSetChanged();
    }

}