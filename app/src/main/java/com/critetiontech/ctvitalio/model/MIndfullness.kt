package com.critetiontech.ctvitalio.model

data class BreathingProtocol(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val benefits: List<String>,
    val icon: Int,
    val hrIncrease: Boolean
)

data class EyeExercise(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val reps: String,
    val benefits: List<String>,
    val icon: Int
)

data class MindfulnessActivity(
    val id: Int,
    val title: String,
    val tag: String,
    val duration: String,
    val description: String,
    val icon: Int
)

data class MindfulnessApiResponse(
    val status: Int,
    val message: String,
    val responseValue: MindfulnessResponseValue
)

data class MindfulnessResponseValue(
    val progress: MindfulnessProgress,
    val categories: List<MindfulnessCategory>
)

data class MindfulnessProgress(
    val activitiesCompleted: Int,
    val dayStreak: Int
)

data class MindfulnessCategory(
    val categoryId: Int,
    val categoryName: String,
    val exercises: List<MindfulnessExercise>
)

data class MindfulnessExercise(
    val exerciseId: Int,
    val exerciseName: String,
    val title: String,
    val subTitle: String,
    val exerciseType: String,
    val description: String,
    val durationMinutes: Int,
    val points: Int,
    val benefits: List<MindfulnessBenefit>
)

data class MindfulnessBenefit(
    val benefitId: Int,
    val benefit: String
)

data class StatusApiResponse(
    val status: Int,
    val message: String,
    val responseValue: List<StatusItem>
)

data class StatusItem(
    val id: Int,
    val remark: String,
    val module: String,
    val status: Int
)