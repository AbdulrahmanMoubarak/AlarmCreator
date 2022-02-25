package com.training.radioalarm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.training.radioalarm.R
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.ui.MainActivity
import com.training.radioalarm.util.Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import kotlin.time.ExperimentalTime

object PlayerNotificationManager {

    fun makeStatusNotification(
        message: String,
        pendingIntent: PendingIntent,
        context: Context,
        channel_id: String,
        channel_name: String,
        title: String
    )
            : NotificationCompat.Builder {
        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = channel_name
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channel_id, name, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, channel_id)
            .setSmallIcon(R.drawable.ic_radio_svgrepo_com)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(null)
            .setSilent(true)


        pendingIntent.let {
            builder.setContentIntent(pendingIntent)
        }

        return builder;

        // Show the notification
        //NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun createImmediateNotification(
        context: Context,
        title: String,
        message: String,
    ) {
        val builder = NotificationCompat.Builder(
            MainApplication.getApplication(),
            Constants.RECORD_CHANNEL_ID
        )

            .setSmallIcon(R.drawable.ic_radio_svgrepo_com)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(PendingIntent.getActivity(
                MainApplication.getAppContext(),
                0,
                Intent(MainApplication.getAppContext(), MainActivity::class.java),
            0
            ))

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }
}