package com.training.radioalarm.util

import android.content.Context
import com.training.radioalarm.application.MainApplication

class SharedPreferenceManager {

    fun setShutDownSharedPreference() {
        val sp =
            MainApplication.getApplication().getSharedPreferences("onLogged", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.apply {
            putInt("UsageType", -1)
        }.apply()

    }

    fun isBroadcastAfterBoot(): Boolean {
        val sp =
            MainApplication.getApplication().getSharedPreferences("onLogged", Context.MODE_PRIVATE)
        val type = sp.getInt("UsageType", -1)
        return type == -1
    }

    fun setNormalUsage() {
        val sp =
            MainApplication.getApplication().getSharedPreferences("onLogged", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.apply {
            putInt("UsageType", 50)
        }.apply()
    }
}