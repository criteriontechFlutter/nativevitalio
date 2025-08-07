package com.critetiontech.ctvitalio.adapter.omronAdapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.UI.OmronActivity.BaseActivity;
import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class WeightScaleDataListAdapter extends RecyclerView.Adapter<WeightScaleDataListAdapter.VersionViewHolder> {
    private Cursor mCursor;

    public WeightScaleDataListAdapter() {

    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vital_data, parent, false);
        return new VersionViewHolder(view);
    }

    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String tvText = "";
        tvText = tvText + "com.critetiontech.ctvitalio.UI.fragments.User  : " +                          mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.DEVICE_SELECTED_USER)) + "\n";
        tvText = tvText + "Start Date  : " +                    setTime(mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_StartTimeKey))) + "\n";
        tvText = tvText + "Weight(Kg)  : " +                    mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_WeightKey)) + "\n";
        tvText = tvText + "Body Fat Level  : " +                mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_BodyFatLevelKey)) + "\n";
        tvText = tvText + "Body Fat Percentage  : " +           mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_BodyFatPercentageKey)) + "\n";
        tvText = tvText + "Resting Metabolism  : " +            mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_RestingMetabolismKey)) + "\n";
        tvText = tvText + "Skeletal Muscle Percentage : " +     mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_SkeletalMuscleKey)) + "\n";
        tvText = tvText + "BMI : " +                            mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_BMIKey)) + "\n";
        tvText = tvText + "Body Age : " +                       mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_BodyAgeKey)) + "\n";
        tvText = tvText + "Visceral Fat Level : " +             mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_VisceralFatLevelKey)) + "\n";
        tvText = tvText + "Visceral Fat Level Classification : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_VisceralFatLevelClassificationKey)) + "\n";
        tvText = tvText + "Skeletal Muscle Level Classification : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_SkeletalMuscleLevelKey))+ "\n";
        tvText = tvText + "BMI Level Classification: " +        mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.WEIGHT_DATA_BMIClassificationKey));
        versionViewHolder.tvData.setText(tvText);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return (mCursor.getCount());
    }

    /**
     * Swaps the Cursor currently held in the adapter with a new one
     * and triggers a UI refresh
     *
     * @param newCursor the new cursor that will replace the existing one
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    public class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvData;

        public VersionViewHolder(View view) {
            super(view);
            tvData = view.findViewById(R.id.tv_data);
        }
    }
    public String setTime(String stringTime){
        BaseActivity baseActivity = new BaseActivity();
        String startTimeString = stringTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Please adjust the format used here to match your data
        Date startTimeDate = null;
        try {
            startTimeDate = dateFormat.parse(startTimeString);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return baseActivity.getDateTime(startTimeDate);
    }
}
