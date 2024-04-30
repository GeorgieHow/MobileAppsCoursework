package com.example.mobileappscoursework.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobileappscoursework.ui.home.HomeFragment
import com.example.mobileappscoursework.ui.leaderboard.LeaderboardFragment
import com.example.mobileappscoursework.ui.groups.GroupsFragment
import com.example.mobileappscoursework.ui.logs.LogFragment
import com.example.mobileappscoursework.ui.profile.ProfileFragment

class NavigationAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> LeaderboardFragment()
            2 -> GroupsFragment()
            3 -> LogFragment()
            4 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}