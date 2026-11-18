package com.personal.nosnooze

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private var pendingHour = 7
    private var pendingMinute = 0
    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        ensureExactAlarmPermission()
        buildUi()
    }

    private fun requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }

    private fun ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!manager.canScheduleExactAlarms()) {
                try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName"))) } catch (_: Exception) { }
            }
        }
    }

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(0xFFF7F8FA.toInt())
        }
        outer.addView(TextView(this).apply {
            text = "NO SNOOZE"; textSize = 14f; setTextColor(0xFF526170.toInt())
        })
        outer.addView(TextView(this).apply {
            text = "Your alarms"; textSize = 30f; setTextColor(0xFF14202B.toInt())
            setPadding(0, 8, 0, 22)
        })
        outer.addView(Button(this).apply {
            text = "+  ADD ALARM"
            setOnClickListener { chooseTime() }
        })
        outer.addView(TextView(this).apply {
            text = "One-time alarms • Local audio • No snooze"
            textSize = 13f; setTextColor(0xFF687785.toInt())
            setPadding(0, 12, 0, 18)
        })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        outer.addView(list)
        setContentView(outer)
        refreshList()
    }

    private fun chooseTime() {
        val now = java.util.Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            pendingHour = h; pendingMinute = m
            val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(picker, 20)
        }, now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE), false).show()
    }

    @Deprecated("Activity result API kept minimal for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            catch (_: Exception) { }
            val item = AlarmItem(AlarmStore.nextId(this), pendingHour, pendingMinute,
                uri.toString(), displayName(uri))
            val items = AlarmStore.all(this)
            items.add(item)
            AlarmStore.save(this, items)
            AlarmScheduler.schedule(this, item)
            refreshList()
            Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayName(uri: Uri): String {
        var name = "Selected audio"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) name = cursor.getString(index) ?: name
        }
        return name
    }

    private fun refreshList() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val items = AlarmStore.all(this).sortedWith(compareBy<AlarmItem> { it.hour }.thenBy { it.minute })
        if (items.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "No alarms yet. Tap ADD ALARM to create one."
                textSize = 16f; setTextColor(0xFF687785.toInt()); setPadding(0, 20, 0, 20)
            })
        }
        items.forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 14, 8, 14)
                setBackgroundColor(-1)
            }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; weightSum = 1f }
            info.addView(TextView(this).apply {
                text = item.displayTime(); textSize = 25f; setTextColor(0xFF14202B.toInt())
            })
            info.addView(TextView(this).apply {
                text = item.audioName; textSize = 12f; setTextColor(0xFF687785.toInt())
            })
            row.addView(info, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(Button(this).apply {
                text = "DELETE"
                setOnClickListener {
                    AlarmScheduler.cancel(this@MainActivity, item.id)
                    AlarmStore.save(this@MainActivity, AlarmStore.all(this@MainActivity).filterNot { it.id == item.id })
                    refreshList()
                }
            })
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            params.bottomMargin = 10
            list.addView(row, params)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (getSystemService(ALARM_SERVICE) as android.app.AlarmManager).canScheduleExactAlarms()) {
            AlarmScheduler.rescheduleAll(this)
        }
        if (::list.isInitialized) refreshList()
    }
}
