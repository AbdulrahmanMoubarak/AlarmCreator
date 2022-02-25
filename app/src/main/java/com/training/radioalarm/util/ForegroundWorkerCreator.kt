package com.training.radioalarm.util

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.workmanager.ForegroundAlarmWorker

class ForegroundWorkerCreator {

    fun createForegroundWorker(){
        val alarm_work_request = OneTimeWorkRequestBuilder<ForegroundAlarmWorker>().apply {
            addTag("foregroundWorker")
            workDataOf(
                "rec_id" to -1,
                "rec_cancel" to -1,
                "rec_time" to -1
            )
            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }

        MainApplication.getAppContext()?.let {
            WorkManager.getInstance(it).enqueue(alarm_work_request.build())
        }
    }

    fun createForegroundAlarmWorker(recording_id: Int, recording_time: Long){
        val alarm_work_request = OneTimeWorkRequestBuilder<ForegroundAlarmWorker>().apply {
            addTag("foregroundWorker")
            setInputData(
                workDataOf(
                    "rec_id" to recording_id,
                    "rec_time" to recording_time
                )
            )
            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }

        MainApplication.getAppContext()?.let {
            WorkManager.getInstance(it).cancelAllWorkByTag("foregroundWorker")
            WorkManager.getInstance(it).enqueue(alarm_work_request.build())
        }
    }


}