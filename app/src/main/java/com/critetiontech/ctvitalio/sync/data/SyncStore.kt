package com.critetiontech.ctvitalio.sync.data

import android.content.Context
import android.content.SharedPreferences
import com.critetiontech.ctvitalio.sync.domain.EveningEntry
import com.critetiontech.ctvitalio.sync.domain.ExperimentRecord
import com.critetiontech.ctvitalio.sync.domain.PatternAnswerRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * The person's own words and taps, kept on the device so an answer is on screen at once, offline
 * included. There is no server yet: when one exists this is the write-ahead copy.
 *
 * Marks hold the event id only. Calendar titles have no field here and so cannot be stored.
 */
class SyncStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sync_record", Context.MODE_PRIVATE)
    private val gson = Gson()

    /** Null means the dummy record has not been seeded for this install yet. */
    var seededOnDay: Int?
        get() = if (prefs.contains(KEY_SEEDED)) prefs.getInt(KEY_SEEDED, 0) else null
        set(value) = prefs.edit().apply {
            if (value == null) remove(KEY_SEEDED) else putInt(KEY_SEEDED, value)
        }.apply()

    var entries: List<EveningEntry>
        get() = readList(KEY_ENTRIES, object : TypeToken<List<EveningEntry>>() {})
        set(value) = write(KEY_ENTRIES, value)

    var markedEventIds: Set<String>
        get() = readList(KEY_MARKS, object : TypeToken<List<String>>() {}).toSet()
        set(value) = write(KEY_MARKS, value.toList())

    var patternAnswers: List<PatternAnswerRecord>
        get() = readList(KEY_PATTERNS, object : TypeToken<List<PatternAnswerRecord>>() {})
        set(value) = write(KEY_PATTERNS, value)

    var experiments: List<ExperimentRecord>
        get() = readList(KEY_EXPERIMENTS, object : TypeToken<List<ExperimentRecord>>() {})
        set(value) = write(KEY_EXPERIMENTS, value)

    var trainingWithdrawn: Boolean
        get() = prefs.getBoolean(KEY_WITHDRAWN, false)
        set(value) = prefs.edit().putBoolean(KEY_WITHDRAWN, value).apply()

    var recordDeleted: Boolean
        get() = prefs.getBoolean(KEY_DELETED, false)
        private set(value) = prefs.edit().putBoolean(KEY_DELETED, value).apply()

    /** One tap from Account, no reason. Everything the person entered goes. */
    fun deleteRecord() {
        recordDeleted = true
        prefs.edit()
            .remove(KEY_ENTRIES)
            .remove(KEY_MARKS)
            .remove(KEY_PATTERNS)
            .remove(KEY_EXPERIMENTS)
            .apply()
    }

    fun clearAll() = prefs.edit().clear().apply()

    private fun <T> readList(key: String, type: TypeToken<List<T>>): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return gson.fromJson(json, type.type) ?: emptyList()
    }

    private fun write(key: String, value: Any) = prefs.edit().putString(key, gson.toJson(value)).apply()

    private companion object {
        const val KEY_SEEDED = "seeded_on_day"
        const val KEY_ENTRIES = "entries"
        const val KEY_MARKS = "marks"
        const val KEY_PATTERNS = "pattern_answers"
        const val KEY_EXPERIMENTS = "experiments"
        const val KEY_WITHDRAWN = "training_withdrawn"
        const val KEY_DELETED = "record_deleted"
    }
}

/**
 * Pilot metrics the spec wants logged from day one, kept as de-identified counts. Nothing here holds
 * a calendar title or an entry's words. Uploading them is waiting on the backend.
 */
class SyncMetrics(context: Context) {
    enum class Event {
        EVENING_ENTRY_ANSWERED,
        READ_BACK_OPENED,
        PATTERN_ANSWERED,
        EXPERIMENT_STARTED,
        EXPERIMENT_STOPPED,
    }

    private val prefs = context.applicationContext.getSharedPreferences("sync_metrics", Context.MODE_PRIVATE)

    fun log(event: Event) = increment("event_${event.name}")

    fun logHomeOpen(hour: Int) = increment("home_open_hour_$hour")

    private fun increment(key: String) = prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
}
