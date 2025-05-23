package com.critetiontech.ctvitalio.UI.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.adapter.omronAdapter.ActivityItemsListAdapter;
import com.critetiontech.ctvitalio.model.ActivityDataItem;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.R;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by Omron HealthCare Inc
 */
public class ActivityItemFragment extends Fragment {

    private Context mContext;

    private final List<ActivityDataItem> activityDataItemArrayList;

    private String localName;

    public ActivityItemFragment() {
        activityDataItemArrayList = new ArrayList<>();
        initList();
    }

    private void initList() {

        ActivityDataItem activityDataItem;
        activityDataItem = new ActivityDataItem(OmronConstants.OMRONActivityData.StepsPerDay, "Steps");
        activityDataItemArrayList.add(activityDataItem);
        activityDataItem = new ActivityDataItem(OmronConstants.OMRONActivityData.AerobicStepsPerDay, "Aerobics Steps");
        activityDataItemArrayList.add(activityDataItem);
        activityDataItem = new ActivityDataItem(OmronConstants.OMRONActivityData.WalkingCaloriesPerDay, "Walking Calories");
        activityDataItemArrayList.add(activityDataItem);
        activityDataItem = new ActivityDataItem(OmronConstants.OMRONActivityData.DistancePerDay, "Distance");
        activityDataItemArrayList.add(activityDataItem);

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ActivityItemFragment newInstance(String localName) {
        ActivityItemFragment fragment = new ActivityItemFragment();
        Bundle args = new Bundle();
        args.putString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            localName = getArguments().getString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        return inflater.inflate(R.layout.fragment_activity_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView lvDevicelist = (RecyclerView) view.findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lvDevicelist.setLayoutManager(linearLayoutManager);
        ActivityItemsListAdapter activityDataListAdapter = new ActivityItemsListAdapter(mContext, (ArrayList<ActivityDataItem>) activityDataItemArrayList, localName);
        lvDevicelist.setAdapter(activityDataListAdapter);
    }


}