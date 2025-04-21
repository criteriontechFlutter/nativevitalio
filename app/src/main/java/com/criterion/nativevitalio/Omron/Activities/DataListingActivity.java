package com.critetiontech.ctvitalio.Omron.Activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.critetiontech.ctvitalio.Omron.fragments.ActivityItemFragment;
import com.critetiontech.ctvitalio.Omron.fragments.RecordsDataFragment;
import com.critetiontech.ctvitalio.Omron.fragments.SleepDataFragment;
import com.critetiontech.ctvitalio.Omron.fragments.VitalDataFragment;
import com.critetiontech.ctvitalio.Omron.utility.Constants;
import com.critetiontech.ctvitalio.R;


/**
 * Created by Omron HealthCare Inc
 */
public class DataListingActivity extends BaseActivity {

    private String localName =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_listing);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        try {
            localName = getIntent().getStringExtra(Constants.extraKeys.KEY_DEVICE_LOCAL_NAME).toLowerCase();
        } catch (Exception e) {
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return VitalDataFragment.newInstance(localName);
                case 1:
                    return SleepDataFragment.newInstance(localName);
                case 2:
                    return RecordsDataFragment.newInstance(localName);
                case 3:
                    return ActivityItemFragment.newInstance(localName);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Blood Pressure";
                case 1:
                    return "Sleep";
                case 2:
                    return "Records";
                case 3:
                    return "Activity";
            }
            return null;
        }
    }
}
