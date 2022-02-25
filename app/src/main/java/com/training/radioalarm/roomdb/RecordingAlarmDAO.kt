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

    @Query ("SELECT * FROM $ALARMS_TABLE_NAME WHERE channel_name = (:channel_name) AND hour = (:hour) AND minute = (:minute)")
    fun findAlarm(channel_name: String, hour: Int, minute: Int): Flow<ChannelRecordingAlarmModel>

    @Query ("SELECT recordingId FROM $ALARMS_TABLE_NAME WHERE channel_name = (:channel_name) AND hour = (:hour) AND minute = (:minute)")
    fun findAlarmId(channel_name: String, hour: Int, minute: Int): Flow<Int>

    @Query ("SELECT * FROM $ALARMS_TABLE_NAME")
    fun getAllAlarms(): Flow<List<ChannelRecordingAlarmModel>>

    @Query("SELECT * FROM $ALARMS_TABLE_NAME WHERE recordingId = (:id)")
    fun findAlarmById(id: Int): Flow<ChannelRecordingAlarmModel>

    @Query("UPDATE $ALARMS_TABLE_NAME SET active = (:active) WHERE recordingId = (:id);")
    fun cancelAlarmById(id: Int, active: Boolean)

    @Query("DELETE FROM $ALARMS_TABLE_NAME WHERE recordingId = (:id)")
    fun deleteAlarmById(id: Int)
}