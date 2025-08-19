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

class TodosFragment : Fragment() {

    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_todos, container, false)
        recycler = view.findViewById(R.id.recyclerCampeoes)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler.setHasFixedSize(true)

        val campeoesOrdenados = CampeoesManager.getAllCampeoes()
            .sortedByDescending { it.Id }

        recycler.adapter = CampeaoAdapter(campeoesOrdenados)

        return view
    }
}
