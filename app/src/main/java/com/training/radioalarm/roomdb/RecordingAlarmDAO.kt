package com.training.radioalarm.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.training.radioalarm.roomdb.model.ChannelRecordingAlarmModel
import com.training.radioalarm.util.Constants.ALARMS_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingAlarmDAO {

    @Insert
    fun insertRecordingAlarm(recording_alarm: ChannelRecordingAlarmModel)

    @Query ("SELECT * FROM $ALARMS_TABLE_NAME WHERE channel_name = (:channel_name) AND time = (:time)")
    fun findAlarm(channel_name: String, time: Long): Flow<ChannelRecordingAlarmModel>

    @Query ("SELECT recordingId FROM $ALARMS_TABLE_NAME WHERE channel_name = (:channel_name) AND time = (:time)")
    fun findAlarmId(channel_name: String, time: Long): Flow<Int>

    @Query ("SELECT * FROM $ALARMS_TABLE_NAME")
    fun getAllAlarms(): Flow<List<ChannelRecordingAlarmModel>>

    @Query("SELECT * FROM $ALARMS_TABLE_NAME WHERE recordingId = (:id)")
    fun findAlarmById(id: Int): Flow<ChannelRecordingAlarmModel>

    @Query("UPDATE $ALARMS_TABLE_NAME SET time = (:new_time) WHERE recordingId = (:id);")
    fun updateAlarmById(id: Int, new_time: Long)

    @Query("DELETE FROM $ALARMS_TABLE_NAME WHERE recordingId = (:id)")
    fun deleteAlarmById(id: Int)
}