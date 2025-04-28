package com.criterion.nativevitalio.Omron.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.criterion.nativevitalio.Omron.Database.OmronDBConstans;
import com.criterion.nativevitalio.Omron.adapter.WeightScaleDataListAdapter;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.Omron.utility.sampleLog;
import com.criterion.nativevitalio.R;

public class WeightScaleDataListingActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "WeightScaleDataListingActivity";
    private RecyclerView mListView;
    private WeightScaleDataListAdapter mWeightScaleDataListAdapter;

    private static final int WEIGHT_LOADER_ID = 7;

    private TextView WeightScaleDataCount;
    private String localName = "";
    private String modelName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_scale_data_listing);

        try {
            localName = getIntent().getStringExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
            modelName = getIntent().getStringExtra(Constants.extraKeys.KEY_MODEL_IDENTIFIER);
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }

        initViews();

        mWeightScaleDataListAdapter = new WeightScaleDataListAdapter();
        mListView.setAdapter(mWeightScaleDataListAdapter);
        getSupportLoaderManager().initLoader(WEIGHT_LOADER_ID, null, this);
    }

    private void initViews() {

        WeightScaleDataCount = (TextView) findViewById(R.id.Weight_Scale_data_count);
        ((TextView) findViewById(R.id.textView)).setText("History - " +  modelName);
        mListView = (RecyclerView) findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(WeightScaleDataListingActivity.this, LinearLayoutManager.VERTICAL, false);
        mListView.setLayoutManager(linearLayoutManager);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(WeightScaleDataListingActivity.this){

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
                    return getContentResolver().query(OmronDBConstans.WEIGHT_DATA_CONTENT_URI,

                            null,
                            OmronDBConstans.DEVICE_LOCAL_NAME + "=? ",
                            new String[]{localName},
                            OmronDBConstans.WEIGHT_DATA_INDEX + " DESC");

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
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        WeightScaleDataCount.setText("Data Count : " + data.getCount());
        mWeightScaleDataListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mWeightScaleDataListAdapter.swapCursor(null);
    }
}