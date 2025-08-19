package com.fallen.wildstats.ui.campeoes

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fallen.wildstats.ui.campeoes.tabs.TodosFragment
import com.fallen.wildstats.ui.campeoes.tabs.AdcFragment
import com.fallen.wildstats.ui.campeoes.tabs.JungleFragment
import com.fallen.wildstats.ui.campeoes.tabs.MidFragment
import com.fallen.wildstats.ui.campeoes.tabs.SuporteFragment
import com.fallen.wildstats.ui.campeoes.tabs.TopFragment

class CampeoesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = listOf(
        TodosFragment(),
        TopFragment(),
        JungleFragment(),
        MidFragment(),
        AdcFragment(),
        SuporteFragment()
    )

    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
