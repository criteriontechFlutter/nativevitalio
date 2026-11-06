package com.critetiontech.ctvitalio.sync

import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.domain.EventMark
import com.critetiontech.ctvitalio.sync.domain.SyncCopy
import com.google.gson.Gson
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Copy and privacy rules that the spec wants enforced in code, not by convention. */
class SyncRulesTest {
    @After
    fun restoreDrafts() {
        SyncCopy.draftsEnabled = true
    }

    // Copy

    @Test
    fun banned_words_never_appear() {
        val banned = Regex(
            "\\b(predict|prevent|detect|monitor|screen|diagnose|burnout|score|streak|goal)\\b",
            RegexOption.IGNORE_CASE,
        )
        assertClean(banned)
    }

    @Test
    fun nothing_syncs_tracks_monitors_or_manages() {
        assertClean(Regex("\\b(syncs|tracks|monitors|manages|syncing|tracking)\\b", RegexOption.IGNORE_CASE))
    }

    @Test
    fun banned_verbs_and_instructions_never_appear() {
        assertClean(Regex("\\b(drains|damages|stresses|burns)\\b", RegexOption.IGNORE_CASE))
        assertClean(Regex("you should|try to|remember to|we noticed", RegexOption.IGNORE_CASE))
    }

    @Test
    fun work_is_never_named_as_a_cause() {
        assertClean(Regex("work (raised|caused)|your job|what work does", RegexOption.IGNORE_CASE))
    }

    private fun assertClean(pattern: Regex) {
        for ((key, entry) in SyncCopy.table) {
            assertFalse("$key: ${entry.text}", pattern.containsMatchIn(entry.text))
        }
    }

    @Test
    fun only_drifting_and_recovering_leads_are_approved() {
        val approved = SyncCopy.table.filterValues { it.approved }.keys
        assertEquals(setOf("state.drifting.lead", "state.recovering.lead"), approved)
    }

    @Test
    fun drafts_stay_off_screen_when_the_flag_is_off() {
        SyncCopy.draftsEnabled = false
        assertEquals("", SyncCopy.text("state.in_sync.lead"))
        assertEquals(
            "The last two weeks have run heavier than your usual.",
            SyncCopy.text("state.drifting.lead"),
        )
    }

    @Test(expected = IllegalStateException::class)
    fun a_string_outside_the_table_cannot_render() {
        SyncCopy.text("not.in.the.table")
    }

    @Test
    fun sync_code_sets_no_text_outside_the_table() {
        val root = File("src/main/java/com/critetiontech/ctvitalio/sync/ui")
        assertTrue("Run from the app module: ${root.absolutePath}", root.isDirectory)
        // The debug preview panel is developer tooling and never ships.
        val literal = Regex("\\.text\\s*=\\s*\"[^\"]*[A-Za-z][^\"]*\"")
        root.listFiles()!!.filter { it.name != "SyncPreviewPanel.kt" }.forEach { file ->
            val match = literal.find(file.readText())
            assertFalse("${file.name}: ${match?.value}", match != null)
        }
    }

    // Privacy

    @Test
    fun a_calendar_title_never_reaches_a_request_a_log_or_a_mark() {
        val gson = Gson()
        val marks = EventMark("evt-review", 0L)
        assertFalse(gson.toJson(marks).contains("Q4"))
        for (event in SyncFixtures.calendar()) {
            assertFalse(event.title, gson.toJson(event.toServer()).contains(event.title))
            assertFalse(event.title, event.toServer().toString().contains(event.title))
            assertFalse(event.title, event.toString().contains(event.title))
        }
    }

    @Test
    fun sync_ui_never_logs() {
        val roots = listOf("ui", "data", "domain").map {
            File("src/main/java/com/critetiontech/ctvitalio/sync/$it")
        }
        val logging = Regex("\\bLog\\.[a-z]+\\(|\\bprintln\\(|printStackTrace\\(")
        roots.flatMap { it.listFiles()!!.toList() }.forEach { file ->
            assertFalse(file.name, logging.containsMatchIn(file.readText()))
        }
    }

    @Test
    fun sync_follows_the_vitalio_theme_and_keeps_one_attention_colour() {
        val colors = File("src/main/res/values/sync_colors.xml").readText()
        for (reference in listOf("@color/primaryBlue", "@color/dashbg", "@color/contbg", "@color/themeTextColorBW")) {
            assertTrue(reference, colors.contains(reference))
        }
        assertTrue(colors.contains("sync_attention"))
        val attentionUses = File("src/main/java/com/critetiontech/ctvitalio/sync/ui").listFiles()!!
            .flatMap { file -> file.readLines().filter { "attention" in it && "R.color" !in it } }
            .filter { "setBackgroundColor(attention)" in it }
        assertEquals("the attention colour is for the Out of sync tick only", 1, attentionUses.size)
    }
}
