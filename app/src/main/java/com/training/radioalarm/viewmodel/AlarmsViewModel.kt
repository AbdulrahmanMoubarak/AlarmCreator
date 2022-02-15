package com.training.radioalarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.broadcast.ChannelAlarmBroadcastReciever
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.roomdb.model.RadioChannelModel
import com.training.radioalarm.util.Constants
import com.training.radioalarm.workManager.AlarmWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AlarmsViewModel: ViewModel() {

    private var database: RecordingAlarmsDatabase? = null

    init {
        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }
    }

    fun scheduleAlarmWorker(calendar: Calendar, duration: Int, channel: RadioChannelModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CoroutineScope(Dispatchers.IO).launch {
                database?.insertRecordingAlarm(
                    ChannelRecordingAlarmModel(
                        0,
                        calendar.timeInMillis,
                        duration,
                        channel.name,
                        channel.uri
                    )
                )?.collect {
                    val recording_id = it


                    val initial_delay = calendar.timeInMillis - Calendar.getInstance().timeInMillis
                    val alarm_work_request = OneTimeWorkRequestBuilder<AlarmWork>().apply {
                        setInputData(
                            workDataOf("rec_id" to recording_id)
                        )
                        addTag("repeating_alarm")
                        setInitialDelay(initial_delay, TimeUnit.MILLISECONDS)
                    }

                    MainApplication.getAppContext()?.let {
                        WorkManager.getInstance(it).enqueue(alarm_work_request.build())
                    }
                    /*recording_id.let {
                        val startIntent = getAlarmIntent(Constants.SET_ALARM_ACTION, it)
                        MainApplication.getAppContext()?.sendBroadcast(startIntent)
                    }*/

                }
            }
        }
    }

    /*
    private fun getAlarmIntent(action_type: String, alarm_id: Int): Intent {
       return Intent(
            MainApplication.getAppContext(),
            ChannelAlarmBroadcastReciever::class.java
        ).apply {
            action = action_type
            setPackage(alarm_id.toString() + "#0")
            putExtra("id", alarm_id)
        }
    }
     */
}