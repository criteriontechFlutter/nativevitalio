package com.critetiontech.ctvitalio.sync.ui

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.critetiontech.ctvitalio.sync.data.SyncFixtures

/**
 * Debug-only panel, opened by long-pressing the wordmark, for seeing every state and time of day
 * without waiting for them. Never reachable in a release build, so its text is not product copy.
 */
object SyncPreviewPanel {
    fun show(activity: Activity, vm: SyncViewModel, openStates: () -> Unit) {
        val fixtures = SyncFixtures.Fixture.values().toList()
        val times = PreviewTime.values().toList()
        val items = buildList {
            fixtures.forEach { add("State: ${it.name}") }
            times.forEach { add("Time: ${it.name}") }
            add("Open the States sheet")
            add("Reset dummy data")
        }
        AlertDialog.Builder(activity)
            .setTitle("Sync preview")
            .setItems(items.toTypedArray()) { _, which ->
                when {
                    which < fixtures.size -> vm.setPreview(fixtures[which])
                    which < fixtures.size + times.size -> vm.setPreview(times[which - fixtures.size])
                    which == fixtures.size + times.size -> openStates()
                    else -> vm.resetDummyData()
                }
            }
            .show()
    }
}
