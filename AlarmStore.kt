package com.personal.nosnooze

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AlarmStore {
    private const val PREFS = "alarms"
    private const val KEY = "items"

    fun all(context: Context): MutableList<AlarmItem> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val result = mutableListOf<AlarmItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                result.add(AlarmItem(o.getInt("id"), o.getInt("hour"), o.getInt("minute"),
                    o.getString("uri"), o.optString("name", "Audio file")))
            }
        } catch (_: Exception) { }
        return result
    }

    fun save(context: Context, items: List<AlarmItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().put("id", item.id).put("hour", item.hour)
                .put("minute", item.minute).put("uri", item.audioUri).put("name", item.audioName))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, array.toString()).apply()
    }

    fun nextId(context: Context): Int = (all(context).maxOfOrNull { it.id } ?: 0) + 1
}
