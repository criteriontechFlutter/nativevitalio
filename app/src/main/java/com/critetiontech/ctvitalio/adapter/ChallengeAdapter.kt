package com.critetiontech.vitalioCorporate.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.critetiontech.vitalioCorporate.UI.fragments.JoinedFragments
import com.critetiontech.vitalioCorporate.UI.fragments.NewChallengeFragment

class ChallengesAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> JoinedFragments()
        else -> NewChallengeFragment()
    }
}