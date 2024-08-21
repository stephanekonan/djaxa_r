package com.example.a5equiz.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.models.PieceData
import com.google.android.material.progressindicator.LinearProgressIndicator

class PieceAdapter(private var pieces: List<PieceData>) :
    RecyclerView.Adapter<PieceAdapter.PieceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PieceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_piece, parent, false)
        return PieceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PieceViewHolder, position: Int) {
        val piece = pieces[position]
        holder.bind(piece)
    }

    override fun getItemCount(): Int = pieces.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newPieces: List<PieceData>) {
        pieces = newPieces
        notifyDataSetChanged()
    }

    inner class PieceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titlePiece: TextView = itemView.findViewById(R.id.titlePiece)
        private val createdAtPiece: TextView = itemView.findViewById(R.id.createdAtPiece)
        private val usedQuantityView: TextView = itemView.findViewById(R.id.usedQuantity)
        private val remainingQuantityView: TextView = itemView.findViewById(R.id.remainingQuantity)
        private val prixUnitaire: TextView = itemView.findViewById(R.id.prixUnitaire)

        @SuppressLint("SetTextI18n")
        fun bind(piece: PieceData) {
            titlePiece.text = piece.name
            createdAtPiece.text = piece.createdAt
            prixUnitaire.text = "PU = ${piece.price} F CFA"

            val usedQuantity = piece.usedQuantity
            val remainingQuantity = piece.quantity

            usedQuantityView.text = "Quantité utilisée: $usedQuantity"
            remainingQuantityView.text = "Quantité restante: $remainingQuantity"
        }
    }

}
