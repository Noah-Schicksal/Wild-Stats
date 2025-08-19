package com.fallen.wildstats.ui.tier_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fallen.wildstats.databinding.FragmentTierBinding


class TierFragment : Fragment() {

private var _binding: FragmentTierBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val TierViewModel =
            ViewModelProvider(this).get(TierlistViewModel::class.java)

    _binding = FragmentTierBinding.inflate(inflater, container, false)
    val root: View = binding.root

    val textView: TextView = binding.textTier
    TierViewModel.text.observe(viewLifecycleOwner) {
      textView.text = it
    }
    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}