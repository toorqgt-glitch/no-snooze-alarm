package com.personal.nosnooze

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val audioUri: String,
    val audioName: String
) {
    fun displayTime(): String {
        val h = if (hour % 12 == 0) 12 else hour % 12
        return "%02d:%02d %s".format(h, minute, if (hour < 12) "AM" else "PM")
    }
}
