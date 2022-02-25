package com.training.radioalarm.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.ui.MainActivity
import com.training.radioalarm.util.Constants.NOTIFICATION_MESSAGE
import com.training.radioalarm.util.Constants.RECORDING_NOTIFICATION_TITLE
import com.training.radioalarm.util.Constants.RECORD_CHANNEL_ID
import com.training.radioalarm.util.Constants.RECORD_CHANNEL_NAME
import com.training.radioalarm.util.Constants.RECORD_NOTIFICATION_ID
import com.training.radioalarm.util.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ChannelAlarmService : Service() {

    lateinit var recording: ChannelRecordingAlarmModel
    var successful = true
    var channelName = ""

    private val job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startForegroundService(channelName: String) {
        this.channelName = channelName

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            RECORD_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        val notificationBuilder =
            PlayerNotificationManager.makeStatusNotification(
                NOTIFICATION_MESSAGE + channelName,
                pendingIntent,
                this,
                RECORD_CHANNEL_ID,
                RECORD_CHANNEL_NAME,
                RECORDING_NOTIFICATION_TITLE
            )

        startForeground(RECORD_NOTIFICATION_ID, notificationBuilder.build())

        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("here", " RecordService: onStartCommand() is called ")

        super.onStartCommand(intent, flags, startId)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService("This is an alarm")
        else
            startForeground(2, Notification())

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (successful) {
            MainApplication.getAppContext()?.let {
                PlayerNotificationManager.createImmediateNotification(
                    it,
                    "Alarm",
                    "Alarm launched successfully."
                )
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}
