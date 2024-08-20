package com.example.a5equiz.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.activities.DetailsActivity
import com.example.a5equiz.models.Repair

class RepairAdapter(private val context: Context, private val repairs: List<Repair>) :
    RecyclerView.Adapter<RepairAdapter.RepairViewHolder>() {

    inner class RepairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.customerName)
        val customerPhone: TextView = itemView.findViewById(R.id.phoneCustomer)
        val dateDeposit: TextView = itemView.findViewById(R.id.dateDeposit)
        val descriptionRepair: TextView = itemView.findViewById(R.id.issue)
        val issuePhone: TextView = itemView.findViewById(R.id.issue)
        val marquePhone: TextView = itemView.findViewById(R.id.phoneModel)
        val montantNegociePiece: TextView = itemView.findViewById(R.id.montantNegocie)
        val montantNormalPiece: TextView = itemView.findViewById(R.id.montantNormal)
        val pieceId: TextView = itemView.findViewById(R.id.pieceId)
        val repairId: TextView = itemView.findViewById(R.id.repairId)
        val status: TextView = itemView.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return RepairViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
        val repair = repairs[position]

        holder.customerName.text = repair.customerName
        holder.customerPhone.text = repair.customerPhone
        holder.dateDeposit.text = repair.dateDeposit
        holder.descriptionRepair.text = repair.descriptionRepair
        holder.issuePhone.text = repair.issuePhone
        holder.marquePhone.text = repair.marquePhone
        holder.montantNegociePiece.text = repair.montantNegociePiece.plus(" F CFA")
        holder.montantNormalPiece.text = repair.montantNormalPiece.plus(" F CFA")
        holder.pieceId.text = repair.pieceId
        holder.repairId.text = repair.repairId
        holder.status.text = repair.status

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("REPAIR_ID", repair.repairId)
                putExtra("PIECE_ID", repair.pieceId)
                putExtra("CUSTOMER_NAME", repair.customerName)
                putExtra("CUSTOMER_PHONE", repair.customerPhone)
                putExtra("DATE_DEPOSIT", repair.dateDeposit)
                putExtra("DESCRIPTION_REPAIR", repair.descriptionRepair)
                putExtra("ISSUE_PHONE", repair.issuePhone)
                putExtra("MARQUE_PHONE", repair.marquePhone)
                putExtra("MONTANT_NEGOCIE", repair.montantNegociePiece)
                putExtra("MONTANT_NORMAL", repair.montantNormalPiece)
                putExtra("STATUS", repair.status)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = repairs.size

}