package com.critetiontech.ctvitalio.adapter.omronAdapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;



import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.R;
import com.critetiontech.ctvitalio.UI.OmronActivity.BaseActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by Omron HealthCare Inc
 */

public class PulseOxymeterDataListAdapter extends RecyclerView.Adapter<PulseOxymeterDataListAdapter.VersionViewHolder> {

    private Cursor mCursor;


    public PulseOxymeterDataListAdapter() {
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
        tvText = tvText + "User  : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.DEVICE_SELECTED_USER)) + "\n";
        tvText = tvText + "Start Date  : " + setTime(mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.OXYMETER_DATA_StartTimeKey))) + "\n";
        tvText = tvText + "SpO2 " + " (%) : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.OXYMETER_DATA_SpO2Key)) + "\n";
        tvText = tvText + "Pulse " + " (bpm)  : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.OXYMETER_DATA_PulseKey));
        versionViewHolder.tvData.setText(tvText);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return (mCursor.getCount());
    }

    @SuppressLint("Range")
    @Override
    public long getItemId(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IndexOutOfBoundsException("Unable to move cursor to position " + position);
        }
        return mCursor.getInt(mCursor.getColumnIndex(OmronDBConstans.OXYMETER_DATA_INDEX));
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

    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvData;

        VersionViewHolder(View view) {
            super(view);
            tvData = (TextView) view.findViewById(R.id.tv_data);
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
