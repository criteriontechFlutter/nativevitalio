package com.criterion.nativevitalio.Omron.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.criterion.nativevitalio.Omron.Database.OmronDBConstans;
import com.criterion.nativevitalio.Omron.adapter.PulseOxymeterDataListAdapter;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.Omron.utility.sampleLog;
import com.criterion.nativevitalio.R;

/**
 * Created by Omron HealthCare Inc
 */
public class PulseOxymeterDataListingActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "PulseOxymeterDataListingActivity";
    private RecyclerView mListView;
    private PulseOxymeterDataListAdapter mOxymeterDataListAdapter;

    private static final int OXYMETER_LOADER_ID = 8;

    private TextView OxymeterDataCount;
    private String localName = "";
    private String modelName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_oxymeter_data_listing);

        try {
            localName = getIntent().getStringExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
            modelName = getIntent().getStringExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER);
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }

        initViews();

        mOxymeterDataListAdapter = new PulseOxymeterDataListAdapter();
        mListView.setAdapter(mOxymeterDataListAdapter);
        getSupportLoaderManager().initLoader(OXYMETER_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(OXYMETER_LOADER_ID);
    }

    private void initViews() {

        OxymeterDataCount = (TextView) findViewById(R.id.oxymeter_data_count);
        mListView = (RecyclerView) findViewById(R.id.lv_devicelist);
        ((TextView) findViewById(R.id.textView)).setText("History - " +  modelName);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(PulseOxymeterDataListingActivity.this, LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(PulseOxymeterDataListingActivity.this) {

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
                    return getContentResolver().query(OmronDBConstans.OXYMETER_DATA_CONTENT_URI,
                            null,
                            OmronDBConstans.DEVICE_LOCAL_NAME + "=? ",
                            new String[]{localName},
                            OmronDBConstans.OXYMETER_DATA_INDEX + " DESC");

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

        OxymeterDataCount.setText("Data Count : " + data.getCount());
        mOxymeterDataListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mOxymeterDataListAdapter.swapCursor(null);
    }
}
