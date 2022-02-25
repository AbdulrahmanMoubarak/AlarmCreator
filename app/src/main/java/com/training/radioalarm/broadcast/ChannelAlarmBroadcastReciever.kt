package com.training.radioalarm.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.service.ChannelAlarmService
import com.training.radioalarm.util.Constants
import com.training.radioalarm.util.ForegroundWorkerCreator
import com.training.radioalarm.util.PlayerNotificationManager
import com.training.radioalarm.util.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.*

class ChannelAlarmBroadcastReciever : BroadcastReceiver() {

    private var database: RecordingAlarmsDatabase? = null

    override fun onReceive(context: Context?, intent: Intent?) {

        var actionStr = ""
        intent?.action?.let { actionStr = it }
        var id = -1
        intent?.`package`?.toInt()?.let{id = it}


        if (actionStr.contains(Constants.START_SERVICE_ACTION)) {
            startServiceByAlarmId(id, context)
            return
        }


        if (actionStr.equals("StopForeground")) {
            context?.let {
                WorkManager.getInstance(it).cancelAllWorkByTag("foregroundWorker")
            }
            return
        }

        if (
            actionStr.equals(Intent.ACTION_BOOT_COMPLETED) ||
            actionStr.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)
        ) {
            ForegroundWorkerCreator().createForegroundWorker()
            resetAllAlarms()
            return
        }
    }

    private fun resetAllAlarms() {
        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }
        CoroutineScope(Dispatchers.IO).launch {
            database?.getAllAlarms()?.collect {
                val alarmList = it
                for (alarm in alarmList) {
                    val now = Calendar.getInstance()
                    val alarmCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR, alarm.hour)
                        set(Calendar.MINUTE, alarm.minute)
                    }
                    if (alarmCal.timeInMillis < now.timeInMillis) {
                        MainApplication.getAppContext()?.let {
                            PlayerNotificationManager.createImmediateNotification(
                                it,
                                "Alarm",
                                "You missed some alarms."
                            )
                        }
                        alarmCal.add(Calendar.HOUR, 24)
                        ForegroundWorkerCreator().createForegroundAlarmWorker(
                            alarm.recordingId,
                            alarmCal.timeInMillis
                        )
                    } else {
                        ForegroundWorkerCreator().createForegroundAlarmWorker(
                            alarm.recordingId,
                            alarmCal.timeInMillis
                        )
                    }
                }
            }
        }
    }

    private fun startServiceByAlarmId(id: Int, context: Context?){
        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }

        val serviceIntent = Intent(context, ChannelAlarmService::class.java).apply {
        }

        CoroutineScope(Dispatchers.IO).launch {
            database?.findAlarmById(id)?.collect {
                if (it != null) {
                    if(it.active) {
                        context?.startService(serviceIntent)
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.HOUR, 24);
                        ForegroundWorkerCreator().createForegroundAlarmWorker(
                            id,
                            calendar.timeInMillis
                        )
                        SharedPreferenceManager().setNormalUsage()
                    }
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

    /*
    MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
            return
        }

        val backup_channelId = intent?.`package`?.split('#')?.get(0)
        val channelId = backup_channelId?.toInt()

        if (channelId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                database?.findAlarmById(channelId)?.collect {
                    val recording = it
                }
            }
        }
     */
}