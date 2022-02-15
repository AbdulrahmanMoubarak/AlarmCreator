package com.training.radioalarm.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
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
import com.training.radioalarm.R
import com.training.radioalarm.roomdb.model.RadioChannelModel
import com.training.radioalarm.util.Constants.APP_IN_BATTERY_OPTIMIZATION
import com.training.radioalarm.util.Constants.DEVICE_ABLE_TO_RECORD
import com.training.radioalarm.util.Constants.DEVICE_IN_POWER_SAVING_MODE
import com.training.radioalarm.viewmodel.AlarmsViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var alarmViewmodel: AlarmsViewModel
    private var channel = RadioChannelModel(
        "https://mypromotions.sfo2.cdn.digitaloceanspaces.com/radios/41471.jpg",
        "El-Radio-9090 (الراديو٩٠٩٠)",
        "https://9090streaming.mobtada.com/9090FMEGYPT",
        41471,
        "eg",
        "Adult Contemporary,Culture,Talk"
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val granted = checkPermission()


        alarmViewmodel  = ViewModelProvider(this).get(AlarmsViewModel::class.java)

        pick_btn_main.setOnClickListener {
            val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkBatteryOptimization()
            } else {
                DEVICE_ABLE_TO_RECORD
            }
            if(state == DEVICE_ABLE_TO_RECORD) {
                val dialog = RecordTimeDialogFragment(::onDialogConfirmVm)
                dialog.show(
                    supportFragmentManager,
                    "record start time and duration"
                )
            } else if (state == APP_IN_BATTERY_OPTIMIZATION){
                val dialog = AlertDialog.Builder(this).apply {
                    setTitle("Can't set alarm !!")
                    setMessage("App is in battery optimiztion, please change the state to be able to set the alarm.")
                    setPositiveButton("OK"){dialogInterface, i ->
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        } else {
                            TODO("VERSION.SDK_INT < M")
                        }
                        startActivity(intent)
                    }
                    setNegativeButton("Cancel"){dialogInterface, i ->
                    }
                }
                dialog.show()
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermission(): Boolean{
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SET_ALARM
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            else -> {
                return requestPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission():Boolean{
        var granted = false
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                granted = isGranted
            }


            requestPermissionLauncher.launch(Manifest.permission.SET_ALARM)

        return granted
    }
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