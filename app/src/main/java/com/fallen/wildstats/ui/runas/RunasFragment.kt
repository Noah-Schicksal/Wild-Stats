package com.fallen.wildstats.ui.runas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fallen.wildstats.databinding.FragmentRunasBinding
import com.fallen.wildstats.ui.inicio.RunasViewModel

class RunasFragment : Fragment() {

    private var _binding: FragmentRunasBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val RunasViewModel =
            ViewModelProvider(this).get(RunasViewModel::class.java)

        _binding = FragmentRunasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRunas
        RunasViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}