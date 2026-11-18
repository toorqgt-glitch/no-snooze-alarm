package com.personal.nosnooze

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

class AlarmPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            stopPlayback()
            return START_NOT_STICKY
        }
        val uriText = intent?.getStringExtra("audio_uri") ?: run { stopSelf(); return START_NOT_STICKY }
        val time = intent.getStringExtra("alarm_time") ?: "Alarm"
        createChannel()
        val cancelIntent = PendingIntent.getService(this, 991,
            Intent(this, AlarmPlaybackService::class.java).setAction(ACTION_CANCEL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val fullScreen = PendingIntent.getActivity(this, 992,
            Intent(this, AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Alarm ringing")
                .setContentText(time)
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setFullScreenIntent(fullScreen, true)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "CANCEL", cancelIntent)
                .build()
        } else Notification.Builder(this).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm ringing").setContentText(time).setOngoing(true).build()

        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION_ID, notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(NOTIFICATION_ID, notification)

        try {
            wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoSnoozeAlarm:Playback").apply {
                    setReferenceCounted(false); acquire(4 * 60 * 60 * 1000L)
                }
            player?.release()
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(this@AlarmPlaybackService, Uri.parse(uriText))
                isLooping = false
                setOnCompletionListener { stopPlayback() }
                setOnErrorListener { _, _, _ -> stopPlayback(); true }
                prepare()
                start()
            }
        } catch (_: Exception) {
            stopPlayback()
        }
        return START_NOT_STICKY
    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "Alarms", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Alarm playback controls"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun stopPlayback() {
        try { player?.stop() } catch (_: Exception) { }
        player?.release(); player = null
        sendBroadcast(Intent(ACTION_FINISHED).setPackage(packageName))
        stopForeground(STOP_FOREGROUND_REMOVE)
        wakeLock?.let { if (it.isHeld) it.release() }; wakeLock = null
        stopSelf()
    }

    override fun onDestroy() {
        player?.release(); player = null
        wakeLock?.let { if (it.isHeld) it.release() }; wakeLock = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_CANCEL = "com.personal.nosnooze.CANCEL"
        const val ACTION_FINISHED = "com.personal.nosnooze.FINISHED"
        private const val CHANNEL = "alarm_playback"
        private const val NOTIFICATION_ID = 42
    }
}
