package com.critetiontech.ctvitalio.UI.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.UI.OmronActivity.ActivityListingActivity;
import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.adapter.omronAdapter.ActivityDataListAdapter;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.R;


/**
 * Created by Omron HealthCare Inc
 */

public class ActivityDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ActivityDataListAdapter activityDataListAdapter;

    public static final int ACTIVITY_LOADER_ID = 2;

    private String mItemType;

    private String localName;

    public ActivityDataFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ActivityDataFragment newInstance(String type, String localName) {
        ActivityDataFragment fragment = new ActivityDataFragment();
        Bundle args = new Bundle();
        args.putString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
        args.putString(Constants.bundleKeys.KEY_ACTIVITY_DATA_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_data, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            localName = getArguments().getString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
            mItemType = getArguments().getString(Constants.bundleKeys.KEY_ACTIVITY_DATA_TYPE);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView lvDevicelist = view.findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lvDevicelist.setLayoutManager(linearLayoutManager);
        Context mContext = getActivity();


        activityDataListAdapter = new ActivityDataListAdapter(mContext, new ActivityDataListAdapter.ActivityDaySelect() {
            @Override
            public void onItemSelect(String _localName, String type, String date, String sequenceNo) {

                Intent toVitalData = new Intent(getActivity(), ActivityListingActivity.class);
                toVitalData.putExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME,_localName);
                toVitalData.putExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_TYPE,type);
                toVitalData.putExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_SEQ,sequenceNo);
                toVitalData.putExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_DATE,date);

                startActivity(toVitalData);
            }
        });

        lvDevicelist.setAdapter(activityDataListAdapter);
        getLoaderManager().initLoader(ACTIVITY_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {


            private Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getActivity().getContentResolver().query(OmronDBConstans.ACTIVITY_DATA_CONTENT_URI,
                            null,
                            OmronDBConstans.ACTIVITY_DATA_Type + "=? AND " + OmronDBConstans.DEVICE_LOCAL_NAME + "=? ",
                            new String[]{mItemType, localName},
                            OmronDBConstans.ACTIVITY_DATA_StartDateUTCKey + " DESC");

                } catch (Exception e) {
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null) {
            activityDataListAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        activityDataListAdapter.swapCursor(null);
    }
}