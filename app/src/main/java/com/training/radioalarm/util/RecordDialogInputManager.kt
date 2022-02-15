package com.training.radioalarm.util

import android.widget.Toast
import com.training.radioalarm.application.MainApplication

class RecordDialogInputManager {

    fun validateDialogInput(start: String, duration: String): Int{
        if(start == "" || duration == ""){
            Toast.makeText(
                MainApplication.getAppContext(),
                "You have to set both start time and duration",
                Toast.LENGTH_SHORT
            ).show()
            return -1
        } else if(duration.toInt() > 120){
            Toast.makeText(
                MainApplication.getAppContext(),
                "Duration must be less than 120 minutes",
                Toast.LENGTH_SHORT
            ).show()
            return -1
        } else {
            return duration.toInt()
        }
    }

    fun convertClockToText(hour: Int, min: Int): String{
        val minStr = if (min < 10) "0${min}" else min.toString()
        val hourStr = if (hour < 10) "0${hour}" else hour.toString()
        return "${hourStr}:${minStr}"
    }
}