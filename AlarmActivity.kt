package com.personal.nosnooze

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlarmActivity : Activity() {
    private val finished = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) setShowWhenLocked(true)
        if (Build.VERSION.SDK_INT >= 27) setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
            setBackgroundColor(0xFF101820.toInt())
        }
        layout.addView(TextView(this).apply {
            text = "ALARM"; textSize = 18f; setTextColor(0xFFB8C7D9.toInt())
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(this).apply {
            text = "Wake up"; textSize = 42f; setTextColor(-1)
            gravity = Gravity.CENTER; setPadding(0, 24, 0, 40)
        })
        layout.addView(Button(this).apply {
            text = "CANCEL"
            textSize = 18f
            setOnClickListener {
                startService(Intent(this@AlarmActivity, AlarmPlaybackService::class.java)
                    .setAction(AlarmPlaybackService.ACTION_CANCEL))
                finish()
            }
        })
        setContentView(layout)
        val filter = IntentFilter(AlarmPlaybackService.ACTION_FINISHED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(finished, filter, RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(finished, filter)
    }

    override fun onDestroy() {
        try { unregisterReceiver(finished) } catch (_: Exception) { }
        super.onDestroy()
    }
}
