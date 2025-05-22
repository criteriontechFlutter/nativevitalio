package com.critetiontech.ctvitalio.UI.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.adapter.omronAdapter.VitalDataListAdapter;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.R;


/**
 * Created by Omron HealthCare Inc
 */

public class VitalDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private VitalDataListAdapter mVitalDataListAdapter;

    public static final int VITAL_LOADER_ID = 1;

    private TextView tvDataCount;

    private String localName;

    public VitalDataFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static VitalDataFragment newInstance(String localName) {
        VitalDataFragment fragment = new VitalDataFragment();
        Bundle args = new Bundle();
        args.putString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vitial_data, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            localName = getArguments().getString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDataCount = (TextView) view.findViewById(R.id.tv_data_count);
        RecyclerView lvDevicelist = (RecyclerView) view.findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lvDevicelist.setLayoutManager(linearLayoutManager);
        mVitalDataListAdapter = new VitalDataListAdapter();
        lvDevicelist.setAdapter(mVitalDataListAdapter);
        getLoaderManager().initLoader(VITAL_LOADER_ID, null, this);
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
                    return getActivity().getContentResolver().query(OmronDBConstans.VITAL_DATA_CONTENT_URI,
                            null,
                            OmronDBConstans.DEVICE_LOCAL_NAME + "=? ",
                            new String[]{localName},
                            OmronDBConstans.VITAL_DATA_INDEX + " DESC");

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

        tvDataCount.setText("Vital Data Count : " + data.getCount());
        mVitalDataListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVitalDataListAdapter.swapCursor(null);
    }
}