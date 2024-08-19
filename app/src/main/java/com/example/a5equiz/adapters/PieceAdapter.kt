package com.example.a5equiz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.models.PieceData
import com.google.android.material.progressindicator.LinearProgressIndicator

class PieceAdapter(private val pieces: List<PieceData>) :
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

    inner class PieceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titlePiece: TextView = itemView.findViewById(R.id.titlePiece)
        private val progressIndicator: LinearProgressIndicator =
            itemView.findViewById(R.id.progressIndicator)
        private val prixValuePiece: TextView = itemView.findViewById(R.id.prixValuePiece)
        private val devisePiece: TextView = itemView.findViewById(R.id.devisePiece)

        fun bind(piece: PieceData) {
            titlePiece.text = piece.name

            val usedQuantity = piece.usedQuantity
            val price = piece.price.toDouble()

            val progressPercentage =
                if (piece.quantity > 0) (piece.usedQuantity.toDouble() / piece.quantity * 100).toInt() else 0
            progressIndicator.apply {
                max = 100
                progress = progressPercentage
            }

            val totalPrice = usedQuantity * price
            prixValuePiece.text = "${totalPrice.toInt()}"
        }
    }
}
