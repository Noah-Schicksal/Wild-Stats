package com.fallen.wildstats.ui.campeoes.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fallen.wildstats.R
import com.fallen.wildstats.ui.model.Campeao

class CampeaoAdapter(
    private val campeoes: List<Campeao>
) : RecyclerView.Adapter<CampeaoAdapter.CampeaoViewHolder>() {

    inner class CampeaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgCampeao)
        val nome: TextView = itemView.findViewById(R.id.nomeCampeao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampeaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.botao_campeao, parent, false)
        return CampeaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CampeaoViewHolder, position: Int) {
        val campeao = campeoes[position]
        holder.nome.text = campeao.Nome

        Glide.with(holder.itemView.context)
            .load(campeao.imgurl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.img)
    }

    override fun getItemCount(): Int = campeoes.size
}
