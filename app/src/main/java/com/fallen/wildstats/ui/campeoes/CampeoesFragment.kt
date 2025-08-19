package com.fallen.wildstats.ui.campeoes

import com.fallen.wildstats.ui.campeoes.CampeoesPagerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.fallen.wildstats.R
import com.fallen.wildstats.databinding.FragmentCampeoesBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CampeoesFragment : Fragment() {

    private var _binding: FragmentCampeoesBinding? = null
    private val binding get() = _binding!!

    private val tabIcons = listOf(
        R.drawable.ic_todos,
        R.drawable.ic_top,
        R.drawable.ic_jungle,
        R.drawable.ic_mid,
        R.drawable.ic_adc,
        R.drawable.ic_suporte
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCampeoesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupTabs()

        return root
    }

    private fun setupTabs() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.dourado)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.cinza)

        // Adapter do ViewPager2
        binding.viewPager.adapter = CampeoesPagerAdapter(this) // Cada aba: TodosFragment, TopFragment...

        TabLayoutMediator(binding.tabLayoutCampeoes, binding.viewPager) { tab, position ->
            val drawable = AppCompatResources.getDrawable(requireContext(), tabIcons[position])!!
            val wrapped = DrawableCompat.wrap(drawable).mutate()
            tab.icon = wrapped
        }.attach()

        fun animateTint(icon: Drawable?, from: Int, to: Int) {
            if (icon == null) return
            val animator = ValueAnimator.ofObject(ArgbEvaluator(), from, to)
            animator.addUpdateListener { valueAnimator ->
                DrawableCompat.setTint(icon, valueAnimator.animatedValue as Int)
            }
            animator.duration = 150
            animator.start()
        }

        binding.tabLayoutCampeoes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                animateTint(tab.icon, unselectedColor, selectedColor)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                animateTint(tab.icon, selectedColor, unselectedColor)
            }
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Forçar cores iniciais somente depois do mediator
        binding.tabLayoutCampeoes.post {
            for (i in 0 until binding.tabLayoutCampeoes.tabCount) {
                val tab = binding.tabLayoutCampeoes.getTabAt(i)
                val icon = tab?.icon
                val color = if (i == binding.tabLayoutCampeoes.selectedTabPosition) selectedColor else unselectedColor
                icon?.let { DrawableCompat.setTint(it, color) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
