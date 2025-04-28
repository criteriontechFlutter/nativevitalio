package com.criterion.nativevitalio.Omron.models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.criterion.nativevitalio.Omron.Database.OmronDBConstans;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

public class PersonalData {
    private boolean isDataExists = false;
    private int genderValue;
    private int unitValue;
    private String Height;
    private String Weight;
    private String Birthday;
    private int day;
    private int month;
    private int year;
    private String Stride;
    private ContentResolver contentResolver;

    public PersonalData(ContentResolver contentResolver){
        this.contentResolver = contentResolver;
    }
    public boolean isDataExists(){return isDataExists;}
    public int getUnitValue(){
        return unitValue;
    }
    public void setUnitValue(int value){
        unitValue = value;
    }
    public int getGenderValue(){
        return genderValue;
    }
    public void setGenderValue(int value){
        genderValue = value;
    }

    public String getHeight(){
        return Height;
    }
    public void setHeight(String value){
        Height = value;
    }

    public String getWeight(){
        return Weight;
    }
    public void setWeight(String value){
        Weight = value;
    }

    public String getStride(){
        return Stride;
    }
    public void setStride(String value){
        Stride = value;
    }

    public String getBirthday(){
        return Birthday;
    }
    public String getBirthdayNum(){
        String[] dateParts = Birthday.split("/");
        return dateParts[0] + dateParts[1] + dateParts[2];
    }
    public int getDay(){ return day; }
    public int getMonth(){ return month; }
    public int getYear(){ return year; }
    public void setBirthday(String value){
        String[] dateParts = value.split("/");
        year = Integer.parseInt(dateParts[0]);
        month = Integer.parseInt(dateParts[1]);
        day = Integer.parseInt(dateParts[2]);
        Birthday = value;
    }
    public void loadPersonalData() {
        Cursor curs = contentResolver.query(OmronDBConstans.PERSONAL_DATA_CONTENT_URI, null, null, null, OmronDBConstans.PERSONAL_DATA_INDEX + " DESC");
        String _setHeight = "170";
        String _setWeight = "70";
        String _setStride = "80";
        String _setBirthday = "2000/01/01";
        int _setGenderValue = OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female;
        int _setUnitValue = OmronConstants.OMRONDeviceWeightUnit.Kg;
        isDataExists = false;
        if (curs != null && curs.moveToFirst()) {
            isDataExists = true;
            int heightIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Height);
            int weightIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Weight);
            int strideIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Stride);
            int birthdayIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Birthday);
            int genderIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Gender);
            int unitIndex = curs.getColumnIndex(OmronDBConstans.PERSONAL_DATA_Unit);
            if (heightIndex >= 0 && !curs.isNull(heightIndex)) {
                _setHeight = curs.getString(heightIndex);
            }
            if (weightIndex >= 0 && !curs.isNull(weightIndex)) {
                _setWeight = curs.getString(weightIndex);
            }
            if (strideIndex >= 0 && !curs.isNull(strideIndex)) {
                _setStride = curs.getString(strideIndex);
            }
            if (birthdayIndex >= 0 && !curs.isNull(birthdayIndex)) {
                _setBirthday = curs.getString(birthdayIndex);
            }
            if (genderIndex >= 0 && !curs.isNull(genderIndex)) {
                _setGenderValue = Integer.parseInt(curs.getString(genderIndex));
            }
            if (unitIndex >= 0 && !curs.isNull(unitIndex)) {
                _setUnitValue = Integer.parseInt(curs.getString(unitIndex));
            }
            curs.close();
        }
        setHeight(_setHeight);
        setWeight(_setWeight);
        setStride(_setStride);
        setBirthday(_setBirthday);
        setGenderValue(_setGenderValue);
        setUnitValue(_setUnitValue);
    }
    public void savePersonalData() {
        Cursor curs = contentResolver.query(OmronDBConstans.PERSONAL_DATA_CONTENT_URI, null, null, null, OmronDBConstans.PERSONAL_DATA_INDEX + " DESC");
        if(curs != null && curs.moveToFirst()) {//既に存在している場合はdelete
            contentResolver.delete(OmronDBConstans.PERSONAL_DATA_CONTENT_URI, null, null);
        }
        ContentValues cv = new ContentValues();
        cv.put(OmronDBConstans.PERSONAL_DATA_Birthday, Birthday);
        cv.put(OmronDBConstans.PERSONAL_DATA_Height, Height);
        cv.put(OmronDBConstans.PERSONAL_DATA_Weight, Weight);
        cv.put(OmronDBConstans.PERSONAL_DATA_Stride, Stride);
        cv.put(OmronDBConstans.PERSONAL_DATA_Gender, String.valueOf(genderValue));
        cv.put(OmronDBConstans.PERSONAL_DATA_Unit, String.valueOf(unitValue));
        contentResolver.insert(OmronDBConstans.PERSONAL_DATA_CONTENT_URI, cv);
        isDataExists = true;
        if (curs != null) {
            curs.close();
        }
    }
}
