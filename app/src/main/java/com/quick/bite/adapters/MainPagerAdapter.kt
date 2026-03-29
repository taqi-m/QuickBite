package com.quick.bite.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.quick.bite.ui.fragments.HistoryFragment
import com.quick.bite.ui.fragments.HomeFragment
import com.quick.bite.ui.fragments.ProfileFragment
import com.quick.bite.ui.fragments.SearchFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val HOME_POSITION = 0
        const val SEARCH_POSITION = 1
        const val HISTORY_POSITION = 2
        const val PROFILE_POSITION = 3
        const val TOTAL_PAGES = 4
    }

    override fun getItemCount(): Int = TOTAL_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            HOME_POSITION -> HomeFragment()
            SEARCH_POSITION -> SearchFragment()
            HISTORY_POSITION -> HistoryFragment()
            PROFILE_POSITION -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}

