package com.criterion.nativevitalio.Omron.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.criterion.nativevitalio.Omron.Database.OmronDBConstans;
import com.criterion.nativevitalio.Omron.adapter.SettingsListAdapter;
import com.criterion.nativevitalio.Omron.models.ReminderItem;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.R;


import java.util.ArrayList;

/**
 * Created by Omron HealthCare Inc
 */
public class SettingsFragment extends Fragment {

    private RecyclerView lvDevicelist;
    private String localName;

    public SettingsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SettingsFragment newInstance(String localName) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME, localName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_listing, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            localName = getArguments().getString(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvDevicelist = (RecyclerView) view.findViewById(R.id.lv_devicelist);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lvDevicelist.setLayoutManager(linearLayoutManager);
        fetchReminderDetails();
    }

    private void fetchReminderDetails() {
        Cursor curs = getActivity().getContentResolver().query(OmronDBConstans.REMINDER_DATA_CONTENT_URI,
                null,OmronDBConstans.DEVICE_LOCAL_NAME+" COLLATE NOCASE = '"+localName+"'",
                null, null);

        ArrayList<ReminderItem> reminderItems = new ArrayList<>();

        if (curs != null
            && curs.getCount() > 0) {
            curs.moveToFirst();
            do {
                @SuppressLint("Range") String hour = curs.getString(curs.getColumnIndex(OmronDBConstans.REMINDER_DATA_Hour));
                @SuppressLint("Range") String minute = curs.getString(curs.getColumnIndex(OmronDBConstans.REMINDER_DATA_Minute));
                @SuppressLint("Range") String days = curs.getString(curs.getColumnIndex(OmronDBConstans.REMINDER_DATA_Days));
                ReminderItem reminderItem = new ReminderItem(hour, minute, days);
                reminderItems.add(reminderItem);

            } while (curs.moveToNext());
        }

        SettingsListAdapter mSettingsListAdapter = new SettingsListAdapter(reminderItems);
        lvDevicelist.setAdapter(mSettingsListAdapter);


    }


}