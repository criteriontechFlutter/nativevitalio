package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.critetiontech.ctvitalio.model.BingoTask
import com.critetiontech.ctvitalio.storage.BingoPreferences

class BingoViewModel(application: Application) : AndroidViewModel(application) {

    private val _tasks = MutableLiveData<List<BingoTask>>()
    val tasks: LiveData<List<BingoTask>> get() = _tasks

    private val _bingoTrigger = MutableLiveData<Boolean>()
    val bingoTrigger: LiveData<Boolean> get() = _bingoTrigger

    private var completedLinesState = BooleanArray(8) { false }

    init {
        val context = getApplication<Application>().applicationContext
        BingoPreferences.clearAll(context)
        loadTasks()
    }

    fun loadTasks() {
        val context = getApplication<Application>().applicationContext
        val initialTasks = listOf(
            BingoTask(1, "🫁", "Take 3\ndeep breaths", BingoPreferences.getTaskState(context, 1)),
            BingoTask(2, "💧", "Drink a\nglass of", BingoPreferences.getTaskState(context, 2)),
            BingoTask(3, "🏃", "Stretch for\n2 minutes", BingoPreferences.getTaskState(context, 3)),
            BingoTask(4, "📝", "Write down\n3 things", BingoPreferences.getTaskState(context, 4)),
            // Middle tile is FREE SPACE, always completed.
            BingoTask(5, "🌬", "Step outside\nfor fresh", true),
            BingoTask(6, "🎵", "Listen to\ncalming music", BingoPreferences.getTaskState(context, 6)),
            BingoTask(7, "💖", "Compliment\nyourself", BingoPreferences.getTaskState(context, 7)),
            BingoTask(8, "🚫", "Put phone\naway for", BingoPreferences.getTaskState(context, 8)),
            BingoTask(9, "👀", "Notice 5\nthings around You", BingoPreferences.getTaskState(context, 9))
        )

        // Make sure index 4 (task 5) is marked as completed in storage too
        BingoPreferences.saveTaskState(context, 5, true)

        _tasks.value = initialTasks
        checkBingoLines(initialTasks, triggerDialog = false)
    }

    fun toggleTask(position: Int) {
        val currentList = _tasks.value?.toMutableList() ?: return
        if (position < 0 || position >= currentList.size) return
        
        val task = currentList[position]
        if (task.id == 5) return

        val context = getApplication<Application>().applicationContext
        task.completed = !task.completed
        BingoPreferences.saveTaskState(context, task.id, task.completed)
        
        _tasks.value = currentList
        checkBingoLines(currentList, triggerDialog = true)
    }

    private fun checkBingoLines(taskList: List<BingoTask>, triggerDialog: Boolean) {
        val completed = taskList.map { it.completed }
        
        val lines = listOf(
            listOf(0, 1, 2), // Row 1
            listOf(3, 4, 5), // Row 2
            listOf(6, 7, 8), // Row 3
            listOf(0, 3, 6), // Col 1
            listOf(1, 4, 7), // Col 2
            listOf(2, 5, 8), // Col 3
            listOf(0, 4, 8), // Diag 1
            listOf(2, 4, 6)  // Diag 2
        )

        var newBingoDetected = false
        for (i in lines.indices) {
            val line = lines[i]
            val isLineCompleted = line.all { completed[it] }
            if (isLineCompleted && !completedLinesState[i]) {
                newBingoDetected = true
            }
            completedLinesState[i] = isLineCompleted
        }

        if (newBingoDetected && triggerDialog) {
            _bingoTrigger.value = true
        }
    }

    fun resetBingoTrigger() {
        _bingoTrigger.value = false
    }

    fun resetAllTasks() {
        val context = getApplication<Application>().applicationContext
        BingoPreferences.clearAll(context)
        completedLinesState = BooleanArray(8) { false }
        loadTasks()
    }
}
