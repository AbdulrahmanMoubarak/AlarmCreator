package com.training.radioalarm.workmanager

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.training.radioalarm.R
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.broadcast.ChannelAlarmBroadcastReciever
import com.training.radioalarm.util.Constants
import java.util.*

class ForegroundAlarmWorker(context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var notification: Notification

    init {

        MainApplication.getAppContext()?.let {

            val id = "22"
            val title = "Radio Alarm"
            val cancel = "Dismiss"
            // This PendingIntent can be used to cancel the worker
            val intent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent(
                    applicationContext,
                    ChannelAlarmBroadcastReciever::class.java
                ).apply {
                    action = "StopForeground"
                },
                0
            )

            // Create a Notification channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(id, "Alarm Channel")
            }

            notification = NotificationCompat.Builder(it, id)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText("waiting for alarms")
                .setSmallIcon(R.drawable.ic_radio_svgrepo_com)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_delete, cancel, intent)
                .build()
        }

    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(22, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)
        return channelId
    }


    @SuppressLint("RestrictedApi")
    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val future = SettableFuture.create<ForegroundInfo>()


        future.set(ForegroundInfo(22, notification))
        return future
    }

    override fun doWork(): Result {
        createForegroundInfo()?.let {
            setForegroundAsync(it)
        }

        val rec_id = inputData.getInt("rec_id", -1)
        val rec_time = inputData.getLong("rec_time", -1)


        if (rec_id != -1) {
            createRecordingAlarm(rec_id, rec_time)
        }



        while (true) {

        }

        return Result.success()
    }

    fun createRecordingAlarm(recording_id: Int, time: Long) {
        val startIntent = getAlarmIntent(
            Constants.START_SERVICE_ACTION,
            recording_id,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarm = MainApplication.getAppContext()
            ?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                //Calendar.getInstance().timeInMillis + 20000, // for testing
                time,
                startIntent
            )

        } else {
            alarm?.setExact(
                AlarmManager.RTC_WAKEUP,
                time,
                startIntent
            )
        }
    }


    private fun getAlarmIntent(action_type: String, alarm_id: Int, flag: Int): PendingIntent {
        val intent = Intent(
            MainApplication.getAppContext(),
            ChannelAlarmBroadcastReciever::class.java
        ).apply {
            action = action_type
            `package` = alarm_id.toString()
            putExtra("id", alarm_id)
        }
        return PendingIntent.getBroadcast(applicationContext, 0, intent, flag)
    }
}