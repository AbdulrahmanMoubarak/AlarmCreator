package com.training.radioalarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.broadcast.ChannelAlarmBroadcastReciever
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.roomdb.model.RadioChannelModel
import com.training.radioalarm.util.Constants
import com.training.radioalarm.util.ForegroundWorkerCreator
import com.training.radioalarm.workmanager.AlarmWorker
import com.training.radioalarm.workmanager.ForegroundAlarmWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AlarmsViewModel : ViewModel() {

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
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE),
                        duration,
                        channel.name,
                        channel.uri,
                        true
                    )
                )?.collect {
                    val recording_id = it


                    ForegroundWorkerCreator().createForegroundAlarmWorker(recording_id, calendar.timeInMillis)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            MainApplication.getAppContext(),
                            "Alarm set successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /*
    fun createWorkerAlarm(recording_id: Int, recording_time: Long){
        //val initial_delay = calendar.timeInMillis - Calendar.getInstance().timeInMillis
        val alarm_work_request = OneTimeWorkRequestBuilder<AlarmWorker>().apply {
            addTag("alarm_worker_$recording_id")
            setInputData(
                workDataOf(
                    "rec_id" to recording_id,
                    "rec_time" to recording_time
                )
            )
            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }

        MainApplication.getAppContext()?.let {
            WorkManager.getInstance(it).enqueue(alarm_work_request.build())
            Log.d("here", "viewModel: work request added successfully")
        }

    }

    fun createForegroundWorkerAlarm(recording_id: Int, recording_time: Long){
        MainApplication.getAppContext()?.let {
            WorkManager.getInstance(it).cancelAllWorkByTag("foreground_worker")
            val alarm_work_request = OneTimeWorkRequestBuilder<ForegroundAlarmWorker>().apply {
                addTag("foreground_worker")
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                setInputData(
                    workDataOf(
                        "rec_id" to recording_id,
                        "rec_time" to recording_time
                    )
                )
            }
            WorkManager.getInstance(it).enqueue(alarm_work_request.build())
        }
    }

    fun createRecordingAlarm(recording_id: Int, recording_time: Long) {

        val startIntent = getAlarmIntent(Constants.START_SERVICE_ACTION, recording_id)
        val alarm = MainApplication.getAppContext()
            ?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                recording_time,
                startIntent
            )
        } else {
            alarm?.setExact(
                AlarmManager.RTC_WAKEUP,
                recording_time,
                startIntent
            )
        }
    }


    private fun getAlarmIntent(action_type: String, alarm_id: Int): PendingIntent {
        val intent = Intent(
            MainApplication.getAppContext(),
            ChannelAlarmBroadcastReciever::class.java
        ).apply {
            action = action_type
            putExtra("id", alarm_id)
        }
        return PendingIntent.getBroadcast(MainApplication.getAppContext(), 0, intent, 0)
    }

     */
}