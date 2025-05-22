package com.critetiontech.ctvitalio.firebase

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.critetiontech.ctvitalio.MainActivity
import com.critetiontech.ctvitalio.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            showCustomNotification(remoteMessage)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun showCustomNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val customView = RemoteViews(packageName, R.layout.custom_notification_layout)
        customView.setTextViewText(R.id.title, remoteMessage.notification?.title)
        customView.setTextViewText(R.id.body, remoteMessage.notification?.body)

        val notification = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customView)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        // Send token to your backend server
    }
}
