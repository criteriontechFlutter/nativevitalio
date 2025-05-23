package com.critetiontech.ctvitalio.adapter.omronAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.R;
import com.critetiontech.ctvitalio.UI.OmronActivity.BaseActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.VersionViewHolder>{

    private final Context context;
    private Cursor mCursor;


    public ActivityListAdapter(Context _context) {
        this.context = _context;
    }



    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity_listing, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        versionViewHolder.llBg.setBackgroundColor(context.getResources().getColor(R.color.white));

        float measurement = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DIVIDED_DATA_MeasurementValueKey)));

        if(measurement > 0) {
            versionViewHolder.llBg.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }
        String tvText = "";
        tvText = tvText + "Measurement : " + mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DIVIDED_DATA_MeasurementValueKey)) + "\n";
        tvText = tvText + "Start Date : " + getDateAndTIme(mCursor.getString(mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DIVIDED_DATA_StartDateUTCKey)));
        versionViewHolder.tvData.setText(tvText);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return (mCursor.getCount());
    }

    @Override
    public long getItemId(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IndexOutOfBoundsException("Unable to move cursor to position " + position);
        }
        return mCursor.getInt(mCursor.getColumnIndex(OmronDBConstans.VITAL_DATA_INDEX));
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
    private String getDateAndTIme(String timeStamp) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        long startDate = Long.parseLong(timeStamp);

        Date date= new Date(startDate);
//        return formatter.format(date);
        return new BaseActivity().getDateTime(date);
    }

    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvData;
        private final LinearLayout llBg;

        VersionViewHolder(View view) {
            super(view);

            tvData = (TextView) view.findViewById(R.id.tv_measurement);
            llBg = (LinearLayout) view.findViewById(R.id.ll_bg);
        }
    }
}