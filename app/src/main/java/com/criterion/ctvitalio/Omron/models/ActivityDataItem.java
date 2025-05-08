package com.critetiontech.ctvitalio.Omron.models;

/**
 * Created by Omron HealthCare Inc
 */

public class ActivityDataItem {

    private String mKey;
    private String mName;

    public ActivityDataItem(String _mKey, String _mName) {
        this.mKey = _mKey;
        this.mName = _mName;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }


}
