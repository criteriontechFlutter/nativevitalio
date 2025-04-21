package com.critetiontech.ctvitalio.Omron.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.critetiontech.ctvitalio.Omron.Database.OmronDBConstans;
import com.critetiontech.ctvitalio.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Omron HealthCare Inc
 */

public class ActivityDataListAdapter extends RecyclerView.Adapter<ActivityDataListAdapter.VersionViewHolder> {

    private final Context context;
    private Cursor mCursor;

    private ActivityDaySelect mActivityItemSelect;

    public ActivityDataListAdapter(Context _context, ActivityDaySelect activityItemSelect) {
        this.context = _context;
        this.mActivityItemSelect = activityItemSelect;
    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity_data, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int _position) {
        final int position = _position;
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String tvText = "";
        versionViewHolder.llBg.setBackgroundColor(context.getResources().getColor(R.color.white));

        int measurementIndex = mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DATA_MeasurementValueKey);
        if (measurementIndex == -1) {
            // Handle error or return, column not found
            return;
        }
        float measurement = Float.parseFloat(mCursor.getString(measurementIndex));

        if(measurement > 0) {
            versionViewHolder.llBg.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }
        tvText = tvText + "Total Measurement : " + mCursor.getString(measurementIndex) + "\n";

        final int dateIndex = mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DATA_StartDateUTCKey);
        if (dateIndex == -1) {
            // Handle error or return, column not found
            return;
        }
        tvText = tvText + "Date : " + getDateAndTIme(mCursor.getString(dateIndex)) ;

        versionViewHolder.tvData.setText(tvText);

        versionViewHolder.llBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCursor.moveToPosition(position)) {
                    return;
                }

                int localNameIndex = mCursor.getColumnIndex(OmronDBConstans.DEVICE_LOCAL_NAME);
                if (localNameIndex == -1) {
                    // Handle error or return, column not found
                    return;
                }
                String localName = mCursor.getString(localNameIndex);

                int typeIndex = mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DATA_Type);
                if (typeIndex == -1) {
                    // Handle error or return, column not found
                    return;
                }
                String type = mCursor.getString(typeIndex);

                int seqIndex = mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DATA_SeqNumKey);
                if (seqIndex == -1) {
                    // Handle error or return, column not found
                    return;
                }
                String seq = mCursor.getString(seqIndex);

                String date = mCursor.getString(dateIndex);

                mActivityItemSelect.onItemSelect(localName, type, date, seq);
            }
        });
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
        int indexColumn = mCursor.getColumnIndex(OmronDBConstans.ACTIVITY_DATA_INDEX);
        if (indexColumn == -1) {
            // Handle error or return, column not found
            throw new IllegalArgumentException("Column " + OmronDBConstans.ACTIVITY_DATA_INDEX + " not found");
        }
        return mCursor.getInt(indexColumn);
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

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        long startDate = Long.parseLong(timeStamp);

        Date date= new Date(startDate);
        return formatter.format(date);
    }

    public interface ActivityDaySelect {
        void onItemSelect(String localName, String type, String date, String sequenceNo);
    }

    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout llBg;
        private final TextView tvData;

        VersionViewHolder(View view) {
            super(view);
            llBg = (LinearLayout) view.findViewById(R.id.ll_bg);
            tvData = (TextView) view.findViewById(R.id.tv_data);
        }
    }
}