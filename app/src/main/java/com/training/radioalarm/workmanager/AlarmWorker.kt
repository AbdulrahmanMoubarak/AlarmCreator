package com.training.radioalarm.workmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.broadcast.ChannelAlarmBroadcastReciever
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.ExperimentalTime

class AlarmWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("here", "doWork: got in alarm do work")
        val rec_id = inputData.getInt("rec_id",-1)
        val rec_time = inputData.getLong("rec_time", Calendar.getInstance().timeInMillis + 5000)
        if(rec_id == -1){
            return Result.failure()
        } else {
            createRecordingAlarm(rec_id, rec_time)
            return Result.success()
        }
    }

    fun createRecordingAlarm(recording_id: Int, recording_time: Long) {
        val startIntent = getAlarmIntent(Constants.START_SERVICE_ACTION, recording_id)
        val alarm = MainApplication.getAppContext()
            ?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().timeInMillis + 10000, // for testing
                //recording_time,
                startIntent
            )

        }else{
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
        return PendingIntent.getBroadcast(applicationContext,0, intent, 0)
    }
}