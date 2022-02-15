package com.training.radioalarm.roomdb.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.training.radioalarm.util.Constants.ALARMS_TABLE_NAME
import java.io.Serializable

@Entity(tableName = ALARMS_TABLE_NAME)
data class ChannelRecordingAlarmModel(
    @PrimaryKey(autoGenerate = true) val recordingId: Int,
    val time: Long,
    val duration: Int,
    val channel_name: String,
    val channel_url: String
): Serializable