package com.training.radioalarm.util

object Constants {
    const val OUTPUT_PATH = "Recs"
    const val RECORD_NOTIFICATION_ID = 5
    const val RECORD_CHANNEL_ID = "RecordChannel01"
    const val RECORD_CHANNEL_NAME = "RecordChannel01n"
    const val NOTIFICATION_MESSAGE = "Recording channel: "
    const val STOP_SERVICE_ACTION = "STOP_SERVICE"
    const val SET_ALARM_ACTION = "SET_ALARM"
    const val START_SERVICE_ACTION = "START_SERVICE"
    const val DATABASE_NAME = "recordings_database"
    const val ALARMS_TABLE_NAME = "alarms_table"
    const val RECORDING_NOTIFICATION_TITLE="Recording"
    const val ALARM_REPEATING_INTERVAL = 30*60*1000
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_CHANNEL_ID = "Radio"
    const val CHANNEL_NAME = "RadioChannel0"
    const val CHANNEL_ID = "RaidoChannel01"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION=""
    const val NOTIFICATION_TITLE="Playing"

    const val NOTIFICATION_ACTION_PLAY = "play"
    const val NOTIFICATION_ACTION_EXIT = "exit"

    const val APP_IN_BATTERY_OPTIMIZATION = 0
    const val DEVICE_IN_POWER_SAVING_MODE = 1
    const val DEVICE_ABLE_TO_RECORD = 2
}