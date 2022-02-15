package com.training.radioalarm.workManager

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.broadcast.ChannelAlarmBroadcastReciever
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.roomdb.model.RadioChannelModel
import com.training.radioalarm.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AlarmWork(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val rec_id = inputData.getInt("rec_id",-1)
        if(rec_id == -1){
            return Result.failure()
        } else {
            createRecordingAlarm(rec_id)
            return Result.success()
        }
    }

    fun createRecordingAlarm(recording_id: Int?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recording_id?.let {
                val startIntent = getAlarmIntent(Constants.START_SERVICE_ACTION, it)
                MainApplication.getAppContext()?.sendBroadcast(startIntent)
            }
        }
    }


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
}