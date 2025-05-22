package com.critetiontech.ctvitalio.UI.OmronActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.critetiontech.ctvitalio.model.PersonalData;
import com.critetiontech.ctvitalio.utils.sampleLog;
import com.critetiontech.ctvitalio.utils.MyApplication;
;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "BaseActivity";
    private ProgressDialog mProgressDialog;
    static PersonalData personalData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sampleLog.setLoggable(MyApplication.Companion.getAppContext(), Log.INFO);
        super.onCreate(savedInstanceState);
        if(personalData == null){
            personalData = new PersonalData(getContentResolver());
            personalData.loadPersonalData();
        }
        initializeProgressDialog();
    }

    private void initializeProgressDialog() {
        try {
            if (android.os.Build.VERSION.SDK_INT > 10) {
                mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            } else {
                mProgressDialog = new ProgressDialog(this);
            }
            mProgressDialog.setCancelable(false);
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }

    public void showProgressDialog(String message) {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.setMessage(message);
                mProgressDialog.show();
            }
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }

    public void hideProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {
            sampleLog.e(TAG, e);
            e.printStackTrace();
        }
    }

    public Activity getActivity() {
        return this;
    }

    public String getDateTime(Date date){
        Locale currentLocale = MyApplication.Companion.getAppContext().getResources().getConfiguration().locale;
        TimeZone timeZone = TimeZone.getDefault();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale);
        dateFormat.setTimeZone(timeZone);
        String formattedDateTime = dateFormat.format(date);
        return formattedDateTime;
    }

    public String getDate(Date date) {
        Locale currentLocale = MyApplication.Companion.getAppContext().getResources().getConfiguration().locale;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, currentLocale);
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public Date getDateOfBirthDateType(String dateOfBirth, String stringDateFormat){
        SimpleDateFormat dateFormat = new SimpleDateFormat(stringDateFormat);

        try {
            Date date = dateFormat.parse(dateOfBirth);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
