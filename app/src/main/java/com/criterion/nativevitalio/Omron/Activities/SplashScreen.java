package com.criterion.nativevitalio.Omron.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;


import androidx.appcompat.app.AppCompatActivity;

import com.criterion.nativevitalio.Omron.utility.PreferencesManager;
import com.criterion.nativevitalio.Omron.utility.sampleLog;
import com.criterion.nativevitalio.R;
import com.criterion.nativevitalio.utils.MyApplication;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

/**
 * Created by Omron HealthCare Inc
 */
public class SplashScreen extends AppCompatActivity {
    private final static String TAG = "SplashScreen";
    private Context mContext;
    private PreferencesManager preferencesManager = null;
    private boolean isLoadEnd = false;
    private final int keepTime = 1000;
    private final int loadTimeout = 10000;
    private final int checkInterval = 100;
    private CountDownTimer splashScreenFinishListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mContext = this;
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(MyApplication.Companion.getAppContext());
        }
        String apiKey = preferencesManager.getPartnerKey();
        if (apiKey.isEmpty()) {
            isLoadEnd = true;
        }else{
            // OmronConnectivityLibrary initialization and Api key setup.
            OmronPeripheralManager.sharedManager(this).setAPIKey(apiKey, null);
            // Notification Listener for Configuration Availability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(mMessageReceiver,
                        new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification),RECEIVER_NOT_EXPORTED);
            }else{
                mContext.registerReceiver(mMessageReceiver,
                        new IntentFilter(OmronConstants.OMRONBLEConfigurationFileAvailabilityStatusNotification));
            }
        }
        splashScreenFinishListener = new CountDownTimer(loadTimeout, checkInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isLoadEnd) { //Transition when Isloadend is True
                    splashScreenFinishListener.cancel();
                    onFinish();
                }
            }
            @Override
            public void onFinish() {
                Intent intent = new Intent(SplashScreen.this, OmronConnectedDeviceList.class);
                startActivity(intent);
                finish();
            }
        };
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreenFinishListener.start();
            }
        }, keepTime - checkInterval);
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mContext.unregisterReceiver(mMessageReceiver);
            isLoadEnd = true;
            // Get extra data included in the Intent
            final int status = intent.getIntExtra(OmronConstants.OMRONConfigurationStatusKey, 0);

            if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileSuccess) {
                sampleLog.d(TAG, "Config File Extract Success");
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileError) {
                sampleLog.d(TAG, "Config File Extract Failure");
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileUpdateError) {
                sampleLog.d(TAG, "Config File Update Failure");
            }
        }
    };
}
