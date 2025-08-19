package com.fallen.wildstats.ui.campeoes.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fallen.wildstats.R
import com.fallen.wildstats.ui.utils.CampeaoAdapter
import com.fallen.wildstats.ui.utils.CampeoesManager

class JungleFragment : Fragment() {

    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_top, container, false)
        recycler = view.findViewById(R.id.recyclerCampeoesTop)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler.setHasFixedSize(true)

        val topCampeoes = CampeoesManager.getCampeoesByRota("Selva")
            .sortedByDescending { it.Id }

        recycler.adapter = CampeaoAdapter(topCampeoes)

        return view
    }
}
