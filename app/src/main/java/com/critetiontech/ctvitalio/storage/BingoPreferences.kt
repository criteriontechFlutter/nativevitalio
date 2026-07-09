package com.critetiontech.ctvitalio.storage

import android.content.Context

object BingoPreferences {
    private const val PREFS_NAME = "bingo_prefs"
    private const val KEY_PREFIX = "task_"

    fun saveTaskState(context: Context, taskId: Int, completed: Boolean) {
        if (taskId == 5) return // Center tile (id=5) is always completed
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("$KEY_PREFIX$taskId", completed).apply()
    }

    fun getTaskState(context: Context, taskId: Int): Boolean {
        if (taskId == 5) return true
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean("$KEY_PREFIX$taskId", false)
    }

    fun clearAll(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}
