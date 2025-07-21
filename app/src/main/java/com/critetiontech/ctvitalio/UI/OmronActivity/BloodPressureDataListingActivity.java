package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.adapter.omronAdapter.VitalDataListAdapter;
import com.critetiontech.ctvitalio.utils.Constants;
import com.critetiontech.ctvitalio.utils.sampleLog;
import com.critetiontech.ctvitalio.R;

/**
 * Created by Omron HealthCare Inc
 */
public class BloodPressureDataListingActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "VitalDataListingActivity";
    private RecyclerView mListView;
    private VitalDataListAdapter mVitalDataListAdapter;

    private static final int VITAL_LOADER_ID = 1;

    private TextView vitalCount;
    private String localName = "";
    private String modelName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vital_data_listing);

        try {
            localName = getIntent().getStringExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
            modelName = getIntent().getStringExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER);
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }

        initViews();

        mVitalDataListAdapter = new VitalDataListAdapter();
        mListView.setAdapter(mVitalDataListAdapter);
        getSupportLoaderManager().initLoader(VITAL_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(VITAL_LOADER_ID);
    }

    private void initViews() {

        vitalCount = findViewById(R.id.vital_count);
        ((TextView) findViewById(R.id.textView)).setText("History - " +  modelName);
        mListView = findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(BloodPressureDataListingActivity.this, LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(BloodPressureDataListingActivity.this) {

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
                    return getContentResolver().query(OmronDBConstans.VITAL_DATA_CONTENT_URI,
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

        vitalCount.setText("Data Count : " + data.getCount());
        mVitalDataListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVitalDataListAdapter.swapCursor(null);
    }
}
