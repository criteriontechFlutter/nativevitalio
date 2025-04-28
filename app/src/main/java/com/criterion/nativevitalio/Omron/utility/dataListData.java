package com.criterion.nativevitalio.Omron.utility;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dataListData implements Parcelable {
    private List<Map<String, String>> dataList;

    public dataListData(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    private dataListData(Parcel in) {
        dataList = new ArrayList<>();
        in.readList(dataList, getClass().getClassLoader());
    }
    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(dataList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<dataListData> CREATOR = new Creator<dataListData>() {
        public dataListData createFromParcel(Parcel in) {
            return new dataListData(in);
        }

        public dataListData[] newArray(int size) {
            return new dataListData[size];
        }
    };
}
