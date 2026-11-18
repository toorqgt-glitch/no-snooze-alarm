package com.personal.nosnooze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarm_id", -1)
        val item = AlarmStore.all(context).firstOrNull { it.id == id } ?: return
        // This first version treats alarms as one-shot: remove it as it starts ringing.
        AlarmStore.save(context, AlarmStore.all(context).filterNot { it.id == id })
        val service = Intent(context, AlarmPlaybackService::class.java)
            .putExtra("audio_uri", item.audioUri)
            .putExtra("alarm_time", item.displayTime())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(service)
        else context.startService(service)
    }
}
