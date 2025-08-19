package com.fallen.wildstats.ui.utils

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fallen.wildstats.ExibicaoActivity
import com.fallen.wildstats.R
import com.fallen.wildstats.ui.model.Campeao

class CampeaoAdapter(private val campeoes: List<Campeao>) :
    RecyclerView.Adapter<CampeaoAdapter.CampeaoViewHolder>() {

    class CampeaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCampeao)
        val nome: TextView = view.findViewById(R.id.nomeCampeao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampeaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.botao_campeao, parent, false)
        return CampeaoViewHolder(view)
    }

    override fun getItemCount(): Int = campeoes.size

    override fun onBindViewHolder(holder: CampeaoViewHolder, position: Int) {
        val campeao = campeoes[position]
        holder.nome.text = campeao.Nome
        holder.nome.setTextColor(holder.itemView.context.getColor(R.color.dourado))

        // Carregamento direto do bitmap pré-carregado
        val bitmap = CampeoesManager.getBitmap(campeao.Nome)
        if (bitmap != null) {
            holder.img.setImageBitmap(bitmap)
        } else {
            holder.img.setImageResource(R.drawable.place_holder)
        }

        // Clique no item abre ExibicaoActivity com ID do campeão
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ExibicaoActivity::class.java)
            intent.putExtra("campeao_id", campeao.Id) // envia o ID
            context.startActivity(intent)
        }
    }
}
