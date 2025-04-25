package com.critetiontech.ctvitalio.Omron.Activities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.critetiontech.ctvitalio.Omron.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.Omron.adapter.ActivityListAdapter;
import com.critetiontech.ctvitalio.Omron.utility.Constants;
import com.critetiontech.ctvitalio.Omron.utility.sampleLog;
import com.critetiontech.ctvitalio.R;


public class ActivityListingActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final String TAG = "DeviceList";
    private RecyclerView mListView;
    private ActivityListAdapter mVitalDataListAdapter;

    private static final int VITAL_LOADER_ID = 6;
    private String localName = null;
    private String type = null;
    private String seq = null;
    private String date = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);


        try {
            localName = getIntent().getStringExtra(Co   nstants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
            type = getIntent().getStringExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_TYPE);
            seq = getIntent().getStringExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_SEQ);
            date = getIntent().getStringExtra(Constants.bundleKeys.KEY_ACTIVITY_DATA_DATE);

        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }

        Context mContext = this;

        initViews();

        mVitalDataListAdapter = new ActivityListAdapter(mContext);
        mListView.setAdapter(mVitalDataListAdapter);
        getSupportLoaderManager().initLoader(VITAL_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(VITAL_LOADER_ID);
    }

    private void initViews() {

        mListView = (RecyclerView) findViewById(R.id.lv_activitylist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(ActivityListingActivity.this, LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(ActivityListingActivity.this) {

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

                    return getContentResolver().query(OmronDBConstans.ACTIVITY_DIVIDED_DATA_CONTENT_URI,
                            null,
                            OmronDBConstans.DEVICE_LOCAL_NAME + "=? AND " + OmronDBConstans.ACTIVITY_DIVIDED_DATA_Type + "=? AND " + OmronDBConstans.ACTIVITY_DIVIDED_DATA_SeqNumKey + "=? AND " + OmronDBConstans.ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey + "=? ",
                            new String[]{localName, type, seq, date},
                            OmronDBConstans.ACTIVITY_DIVIDED_DATA_INDEX + " ASC");

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

        mVitalDataListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVitalDataListAdapter.swapCursor(null);
    }
}
