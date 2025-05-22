package com.critetiontech.ctvitalio.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Omron HealthCare Inc
 */

public class PeripheralDevice implements Parcelable {

    private String mLocalName;
    private String mUuid;
    private String mModelName;
    private String mModelSeries;
    private int mSelectedUser;
    private int mCategory;

    private int mSequenceNo;

    public PeripheralDevice(String _mLocalName,
                            String _mUuid,
                            String _mModelName,
                            String _mModelSeries,
                            int _mSelectedUser,
                            int _mCategory,
                            int _SequenceNo) {
        this.mLocalName = _mLocalName;
        this.mUuid = _mUuid;
        this.mModelName = _mModelName;
        this.mModelSeries = _mModelSeries;
        this.mSelectedUser = _mSelectedUser;
        this.mCategory = _mCategory;
        this.mSequenceNo = _SequenceNo;
    }

    public PeripheralDevice(String _mLocalName,
                            String _mUuid,
                            String _mModelName,
                            String _mModelSeries,
                            int _mSelectedUser,
                            int _mCategory) {
        this.mLocalName = _mLocalName;
        this.mUuid = _mUuid;
        this.mModelName = _mModelName;
        this.mModelSeries = _mModelSeries;
        this.mSelectedUser = _mSelectedUser;
        this.mCategory = _mCategory;
    }

    public PeripheralDevice(String _mLocalName,
                            String _mUuid,
                            int _mSelectedUser,
                            int _mCategory) {
        this.mLocalName = _mLocalName;
        this.mUuid = _mUuid;
        this.mSelectedUser = _mSelectedUser;
        this.mCategory = _mCategory;
    }


    protected PeripheralDevice(Parcel in) {
        mLocalName = in.readString();
        mUuid = in.readString();
        mModelName = in.readString();
        mModelSeries = in.readString();
        mSelectedUser = in.readInt();
        mCategory = in.readInt();
    }

    public static final Creator<PeripheralDevice> CREATOR = new Creator<PeripheralDevice>() {
        @Override
        public PeripheralDevice createFromParcel(Parcel in) {
            return new PeripheralDevice(in);
        }

        @Override
        public PeripheralDevice[] newArray(int size) {
            return new PeripheralDevice[size];
        }
    };

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String _mUuid) {
        this.mUuid = _mUuid;
    }

    public String getLocalName() {
        return mLocalName;
    }

    public void setLocalName(String _mLocalName) {
        this.mLocalName = _mLocalName;
    }

    public String getModelSeries() {
        return mModelSeries;
    }

    public void setModelName(String _mModelName) {
        this.mModelName = _mModelName;
    }

    public int getSelectedUser() {
        return mSelectedUser;
    }

    public void setSelectedUser(int _mSelectedUser) {
        this.mSelectedUser = _mSelectedUser;
    }

    public String getModelName() {
        return mModelName;
    }

    public void setModelSeries(String _mModelSeries) {
        this.mModelSeries = _mModelSeries;
    }

    public int getCategory() {
        return mCategory;
    }

    public void setCategory(int _mCategory) {
        this.mCategory = _mCategory;
    }
    public int getSequenceNo() {return mSequenceNo;}
    public void setSequenceNo(int SequenceNo){mSequenceNo = SequenceNo;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLocalName);
        dest.writeString(mUuid);
        dest.writeString(mModelName);
        dest.writeString(mModelSeries);
        dest.writeInt(mSelectedUser);
        dest.writeInt(mCategory);
    }
}

