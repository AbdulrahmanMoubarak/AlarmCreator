package com.training.radioalarm.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.training.radioalarm.R
import com.training.radioalarm.application.MainApplication
import com.training.radioalarm.roomdb.RecordingAlarmsDatabase
import com.training.radioalarm.roomdb.model.RadioChannelModel
import com.training.radioalarm.util.Constants.APP_IN_BATTERY_OPTIMIZATION
import com.training.radioalarm.util.Constants.DEVICE_ABLE_TO_RECORD
import com.training.radioalarm.util.Constants.DEVICE_IN_POWER_SAVING_MODE
import com.training.radioalarm.util.ForegroundWorkerCreator
import com.training.radioalarm.util.SharedPreferenceManager
import com.training.radioalarm.viewmodel.AlarmsViewModel
import com.training.radioalarm.workmanager.ForegroundAlarmWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var database: RecordingAlarmsDatabase? = null

    lateinit var alarmViewmodel: AlarmsViewModel
    private var channel = RadioChannelModel(
        "https://mypromotions.sfo2.cdn.digitaloceanspaces.com/radios/41471.jpg",
        "El-Radio-9090 (الراديو٩٠٩٠)",
        "https://9090streaming.mobtada.com/9090FMEGYPT",
        41471,
        "eg",
        "Adult Contemporary,Culture,Talk"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestBackgroundProcessing()

        alarmViewmodel = ViewModelProvider(this).get(AlarmsViewModel::class.java)

        val word = if (SharedPreferenceManager().isBroadcastAfterBoot()) {
            "After boot"
        } else {
            "Normal"
        }

        ForegroundWorkerCreator().createForegroundWorker()
        SharedPreferenceManager().setNormalUsage()


        txtboot.text = word

        showAllAlarms()

        pick_btn_main.setOnClickListener {
            val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkBatteryOptimization()
            } else {
                DEVICE_ABLE_TO_RECORD
            }
            if (state == DEVICE_ABLE_TO_RECORD) {
                val dialog = RecordTimeDialogFragment(::onDialogConfirmVm)
                dialog.show(
                    supportFragmentManager,
                    "record start time and duration"
                )
            } else if (state == APP_IN_BATTERY_OPTIMIZATION) {
                val dialog = AlertDialog.Builder(this).apply {
                    setTitle("Can't set alarm !!")
                    setMessage("App is in battery optimiztion, please change the state to be able to set the alarm.")
                    setPositiveButton("OK") { dialogInterface, i ->
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        } else {
                            TODO("VERSION.SDK_INT < M")
                        }
                        startActivity(intent)
                    }
                    setNegativeButton("Cancel") { dialogInterface, i ->
                    }
                }
                dialog.show()
            }
        }

        cancelbtn.setOnClickListener {
            val id = cancelInput.text.toString()
            if (id != "" && id != null) {
                cancelAlarm(id.toInt())
            }
        }
    }

    private fun onDialogConfirmVm(calendar: Calendar, duration: Int) {
        alarmViewmodel.scheduleAlarmWorker(calendar, duration, channel)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBatteryOptimization(): Int {
        val packageName = getPackageName()
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        if (pm.isPowerSaveMode()) {
            Toast.makeText(
                this,
                "Can't set recorder if device is in power saving mode",
                Toast.LENGTH_SHORT
            ).show()

            return DEVICE_IN_POWER_SAVING_MODE
        }

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            return APP_IN_BATTERY_OPTIMIZATION
        }

        return DEVICE_ABLE_TO_RECORD
    }

    private fun showAllAlarms() {
        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }
        CoroutineScope(Dispatchers.IO).launch {
            database?.getAllAlarms()?.collect {
                for (alarm in it) {
                    if (alarm.active) {
                        withContext(Dispatchers.Main) {
                            alarmstxt.text =
                                alarmstxt.text.toString() + alarm.recordingId.toString() + ","
                        }
                    }
                }
            }
        }
    }

    private fun cancelAlarm(id: Int) {
        MainApplication.getAppContext()?.let {
            database = RecordingAlarmsDatabase.getInstance(it)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                database?.cancelAlarmById(id, false)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Alarm $id canceled", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    txtboot.text = e.message
                }
            }
        }
    }

    private fun requestBackgroundProcessing(){
        val intent = Intent()
        val packageName: String = getPackageName()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pm.isIgnoringBatteryOptimizations(packageName)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        ) intent.action =
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS else {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
    /*
    private fun createForegroundWorker() {
        WorkManager.getInstance(this).cancelAllWorkByTag("foreground_worker")
        val alarm_work_request = OneTimeWorkRequestBuilder<ForegroundAlarmWorker>().apply {
            addTag("foreground_worker")
            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

        }
        WorkManager.getInstance(this).enqueue(alarm_work_request.build())
    }
     */
}

/*
{
	    "image_url":"https://mypromotions.sfo2.cdn.digitaloceanspaces.com/radios/41471.jpg"
	    "name":"El-Radio-9090 (الراديو٩٠٩٠)"
	    "uri":"https://9090streaming.mobtada.com/9090FMEGYPT"
	    "channel_id":41471
	    "countryCode":"eg"
	    "genre":"Adult Contemporary,Culture,Talk"
	}
 */