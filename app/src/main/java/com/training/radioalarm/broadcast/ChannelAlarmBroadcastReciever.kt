package com.training.radioalarm.broadcast

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.service.ChannelAlarmService
import com.training.radioalarm.util.Constants
import com.training.radioalarm.util.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class ChannelAlarmBroadcastReciever : BroadcastReceiver() {

    private var database: RecordingAlarmsDatabase? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context?, intent: Intent?) {

        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }

        var actionStr = ""
        intent?.action?.let { actionStr = it }

        val backup_channelId = intent?.`package`?.split('#')?.get(0)
        val channelId = backup_channelId?.toInt()

        if (channelId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                database?.findAlarmById(channelId)?.collect {
                    val recording = it

                    val serviceIntent = Intent(context, ChannelAlarmService::class.java).apply {
                        putExtra("recording", recording)
                    }

                    if (actionStr.equals(Constants.START_SERVICE_ACTION)) {
                        context?.startService(serviceIntent)
                    }

                    /*
                    if(actionStr.equals(Constants.SET_ALARM_ACTION)){
                        val startPendingIntent = getAlarmIntent(
                            Constants.START_SERVICE_ACTION,
                            recording.recordingId,
                            0
                        )

                        val alarm = MainApplication.getAppContext()
                            ?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                        alarm?.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            recording.time,
                            startPendingIntent
                        )

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                MainApplication.getAppContext(),
                                "Recorder set successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                     */
                }
            }
        }
    }

    private fun isOnlineBySocket(): Flow<Boolean> {
        return flow {
            try {
                val timeoutMs = 1500
                val sock = Socket()
                val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                sock.connect(sockaddr, timeoutMs)
                sock.close()
                emit(true)
            } catch (e: IOException) {
                emit(false)
            }
        }
    }

    fun createRecordingAlarm(recording: ChannelRecordingAlarmModel, intent: Intent?) {
        val interval = (2 * 1000 * 60)

        //updateAlarmInDB(recording, 3*1000*60)
        val newRec = ChannelRecordingAlarmModel(
            0,
            recording.time + interval,
            recording.duration,
            recording.channel_name,
            recording.channel_url
        )

        CoroutineScope(Dispatchers.IO).launch {
            updateAlarmInDB(recording.recordingId, newRec)?.collect { newId ->

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val startPendingIntent =
                        intent?.`package`?.split('#')?.get(1)?.toInt()?.plus(1)?.let {
                            getAlarmIntent(
                                Constants.START_SERVICE_ACTION, newId,
                                it
                            )
                        }
                    val stopPendingIntent =
                        intent?.`package`?.split('#')?.get(1)?.toInt()?.plus(1)?.let {
                            getAlarmIntent(
                                Constants.STOP_SERVICE_ACTION, newId,
                                it
                            )
                        }

                    val alarm = MainApplication.getAppContext()
                        ?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                    alarm?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        newRec.time,
                        startPendingIntent
                    )

                    alarm?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        newRec.time + recording.duration * 60 * 1000,
                        stopPendingIntent
                    )
                }

            }
        }
    }


    private fun getAlarmIntent(action_type: String, alarm_id: Int, uniqueNum: Int): PendingIntent {
        val intent = Intent(
            MainApplication.getAppContext(),
            ChannelAlarmBroadcastReciever::class.java
        ).apply {
            action = action_type
            `package` = alarm_id.toString() + "#" + uniqueNum.toString()
        }
        return PendingIntent.getBroadcast(MainApplication.getAppContext(), 0, intent, 0).apply {

        }
    }

    private suspend fun updateAlarmInDB(
        id: Int,
        recording: ChannelRecordingAlarmModel
    ): Flow<Int>? {
        database?.deleteAlarmbyId(id)
        return database?.insertRecordingAlarm(recording)
    }

    private fun saveUpcomingAlarmID(id: Int) {
        val sp =
            MainApplication.getAppContext()?.getSharedPreferences("onLogged", Context.MODE_PRIVATE)
        val editor = sp?.edit()
        editor?.apply {
            putInt("recording_id", id)
        }?.apply()
    }
}