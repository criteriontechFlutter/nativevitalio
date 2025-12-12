package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.BreathingProtocol
import com.critetiontech.ctvitalio.model.EyeExercise

class WellnessViewModel  (application: Application) : BaseViewModel(application) {
    private val _breathingProtocols = MutableLiveData<List<BreathingProtocol>>()
    val breathingProtocols: LiveData<List<BreathingProtocol>> = _breathingProtocols

    private val _eyeExercises = MutableLiveData<List<EyeExercise>>()
    val eyeExercises: LiveData<List<EyeExercise>> = _eyeExercises

    init {
        loadBreathingProtocols()
        loadEyeExercises()
    }

    private fun loadBreathingProtocols() {
        _breathingProtocols.value = listOf(
            BreathingProtocol(
                id = 1,
                title = "Deep Breathing",
                description = "Reduce Stress",
                duration = "5 mins",
                benefits = listOf("Decrease HR", "Increase HRV"),
                icon = R.drawable.deep_breathing,
                hrIncrease = true
            ),
            BreathingProtocol(
                id = 2,
                title = "Shamanic Breathing",
                description = "Enhanced Creativity",
                duration = "15 mins",
                benefits = listOf("Increase HR", "Decrease HRV"),
                icon = R.drawable.shamanic_breathing,
                hrIncrease = false
            ),
            BreathingProtocol(
                id = 3,
                title = "Box Breathing",
                description = "Enhance Focus",
                duration = "5 mins",
                benefits = listOf("Decrease HR", "Increase HRV"),
                icon = R.drawable.box_breathing,
                hrIncrease = true
            )
        )
    }

    private fun loadEyeExercises() {
        _eyeExercises.value = listOf(
            EyeExercise(
                id = 1,
                title = "Criss-Cross Focus",
                description = "Diagonal eye movements to strengthen eye muscles",
                duration = "2s per movement",
                reps = "8 repetitions",
                benefits = listOf("Strengthens eye muscles", "Improves coordination"),
                icon = R.drawable.criss_cross_focus
            ),
            EyeExercise(
                id = 2,
                title = "Figure 8 Flow",
                description = "Smooth infinity pattern for fluid eye movement",
                duration = "3s per movement",
                reps = "5 repetitions",
                benefits = listOf("Improves tracking", "Enhances flexibility"),
                icon = R.drawable.figure_8_flow
            ),
            EyeExercise(
                id = 3,
                title = "Clock Circle",
                description = "Circular eye movements like reading a clock face",
                duration = "1.5s per movement",
                reps = "6 repetitions",
                benefits = listOf("Increases focus range", "Improves eye coordination"),
                icon = R.drawable.clock_circle
            )
        )
    }
}