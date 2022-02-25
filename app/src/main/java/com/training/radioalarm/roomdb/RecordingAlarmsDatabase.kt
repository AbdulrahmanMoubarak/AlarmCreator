package com.training.radioalarm.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.util.Constants.DATABASE_NAME
import kotlinx.coroutines.flow.Flow

@Database(entities = [ChannelRecordingAlarmModel::class], version = 1)
abstract class RecordingAlarmsDatabase: RoomDatabase(){


    abstract fun dao(): RecordingAlarmDAO

    companion object {
        private var instance: RecordingAlarmsDatabase? = null
        @Synchronized
        fun getInstance(context: Context): RecordingAlarmsDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordingAlarmsDatabase::class.java, DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }


    suspend fun insertRecordingAlarm(recording_alarm: ChannelRecordingAlarmModel): Flow<Int>{
        dao().insertRecordingAlarm(recording_alarm)
        return dao().findAlarmId(recording_alarm.channel_name, recording_alarm.hour, recording_alarm.minute)
    }

    suspend fun findAlarm(channel_name: String, hour: Int, min: Int): Flow<ChannelRecordingAlarmModel>{
        return dao().findAlarm(channel_name, hour, min)
    }

    suspend fun getAllAlarms(): Flow<List<ChannelRecordingAlarmModel>>{
        return dao().getAllAlarms()
    }

    suspend fun findAlarmById(id: Int): Flow<ChannelRecordingAlarmModel>{
        return dao().findAlarmById(id)
    }

    suspend fun cancelAlarmById(id: Int, active: Boolean){
        dao().cancelAlarmById(id, active)
    }

    suspend fun deleteAlarmbyId(id: Int){
        dao().deleteAlarmById(id)
    }

}