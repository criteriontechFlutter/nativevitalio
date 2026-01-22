package com.critetiontech.ctvitalio.utils;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.widget.TextView;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.critetiontech.ctvitalio.R;

import java.time.LocalTime;

/**
 * Created by Omron HealthCare Inc
 */

public class Utilities {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String default_notification_channel_id = "default";

    // Convert lb to Kg
    public static float convertlbToKg(int lb) {
        int kg = lb * 4536;
        return kg / 10000.0f;
    }

    // Convert Feet and Inches to Cm
    public static float convertFeetInchToCm(int feetValue, float inchValue) {

        int gss_arg_value;
        int feet = (int) Math.floor(feetValue);
        short inch = (short) inchValue;
        int us_height_inc = 0;

        if (feet > 0) {
            us_height_inc += feet * 48;
        }
        if (inch > 0) {
            us_height_inc += inch * 4;
        }

        gss_arg_value = us_height_inc * 254;
        gss_arg_value = (gss_arg_value + 100) / 200;
        gss_arg_value = gss_arg_value * 5;

        return (float) (gss_arg_value * 0.1);
    }

    // Round the value
    public static double round(double d, int n) {
        return Math.round(d * Math.pow(10, n)) / Math.pow(10, n);
    }

    // Schedule notification
    public static void scheduleNotification(Notification notification, int delay) {
        Intent notificationIntent = new Intent(MyApplication.Companion.getAppContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(MyApplication.Companion.getAppContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(MyApplication.Companion.getAppContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) MyApplication.Companion.getAppContext().getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    // Get Notification
    public static Notification getNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.Companion.getAppContext(), Utilities.default_notification_channel_id);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        builder.setChannelId(Utilities.NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }

    // Apply gradient color to the text of TextView
    public static void applyGradientTextColor(TextView textView, int startColor, int endColor) {
        // Get the width of the TextView to apply the gradient properly
        float width = textView.getWidth();

        // Define the linear gradient
        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, width, 0f, // from left to right
                startColor, endColor, // gradient start and end colors
                Shader.TileMode.CLAMP // ensures the gradient stretches across the full text
        );

        // Apply the gradient shader to the TextPaint of the TextView
        textView.getPaint().setShader(linearGradient);
    }





}
