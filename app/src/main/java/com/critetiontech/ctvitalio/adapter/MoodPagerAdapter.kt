//package com.critetiontech.ctvitalio.adapter
//
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//import androidx.viewpager2.adapter.FragmentStateAdapter
//import com.critetiontech.ctvitalio.UI.fragments.MoodFragment
//import java.io.Serializable
//
//class MoodPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
//
//    private val moods = listOf(
//        MoodData("Good", "#FFC107", "😊"),
//        MoodData("Happy", "#2196F3", "😄"),
//        MoodData("Sad", "#4CAF50", "😢"),
//        MoodData("Spectacular", "#E91E63", "🤩"),
//        MoodData("Upset", "#3F51B5", "😰"),
//        MoodData("Stressed", "#FF5722", "😤")
//    )
//
//    override fun getItemCount(): Int = moods.size
//
////    override fun createFragment(position: Int): Fragment {
////        return MoodFragment.newInstance(moods[position])
////    }
data class MoodData(
    val name: String,
    val color: String,
    val emoji: String
)