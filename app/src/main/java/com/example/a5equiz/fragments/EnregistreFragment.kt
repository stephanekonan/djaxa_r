package com.example.a5equiz.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.adapters.PieceAdapter
import com.example.a5equiz.bases.BaseFragment
import com.example.a5equiz.models.PieceData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EnregistreFragment : BaseFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var pieceAdapter: PieceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_enregistre, container, false)

        val categoriesContainer: LinearLayout = view.findViewById(R.id.categories_container)
        val addNewPieceBtn: Button = view.findViewById(R.id.addNewPieceBtn)
        recyclerView = view.findViewById(R.id.recyclerView)

        val valueStockTextView: TextView = view.findViewById(R.id.valueStock)
        val valueStockArticle: TextView = view.findViewById(R.id.valueStockArticle)
        val deviseTextView: TextView = view.findViewById(R.id.devise)
        val progressIndicator: LinearProgressIndicator =
            view.findViewById(R.id.LinearProgressIndicator)
        val LinearProgressIndicator2: LinearProgressIndicator =
            view.findViewById(R.id.LinearProgressIndicator2)


        addNewPieceBtn.setOnClickListener {
            val recordFragment = AddPieceFragment()
            recordFragment.show(parentFragmentManager, recordFragment.tag)
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView.layoutManager = LinearLayoutManager(context)
        pieceAdapter = PieceAdapter(emptyList())
        recyclerView.adapter = pieceAdapter

        firestore.collection("categories").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val categoryName = document.getString("name") ?: "Unknown"

                val categoryTextView = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 8.dpToPx(), 0)
                    }
                    text = categoryName
                    setBackgroundResource(R.drawable.badge_background)
                    setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    setTextColor(ContextCompat.getColor(context, R.color.black_primary))
                }

                categoriesContainer.addView(categoryTextView)
            }
        }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }

        fetchPieces()

        val userId = mAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).collection("pieces").get()
                .addOnSuccessListener { pieceDocuments ->
                    var totalValue = 0.0
                    var soldValue = 0.0

                    val tasks = mutableListOf<Task<*>>()
                    for (pieceDoc in pieceDocuments) {
                        val pieceId = pieceDoc.id
                        val piecePrice = pieceDoc.get("price") as? Number ?: 0
                        val pieceQuantity = pieceDoc.getLong("quantity")?.toInt() ?: 0

                        val pieceTotalValue = piecePrice.toDouble() * pieceQuantity
                        totalValue += pieceTotalValue

                        val soldTask = firestore.collection("repairs")
                            .whereArrayContains("partsUsed", pieceId)
                            .get()
                            .addOnSuccessListener { repairDocs ->
                                val soldQuantity = repairDocs.size()
                                soldValue += piecePrice.toDouble() * soldQuantity
                            }
                        tasks.add(soldTask)
                    }

                    Tasks.whenAllSuccess<Void>(tasks).addOnCompleteListener {
                        valueStockTextView.text = formatCurrency(soldValue)
                        deviseTextView.text = "FCFA"

                        val progressPercentage = if (totalValue > 0) {
                            (soldValue / totalValue * 100).toInt()
                        } else {
                            0
                        }
                        progressIndicator.progress = progressPercentage
                        progressIndicator.max = 100
                        progressIndicator.setIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        progressIndicator.trackColor =
                            ContextCompat.getColor(requireContext(), R.color.gray_text)
                    }
                }.addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting pieces data: ", exception)
                }
        }

        if (userId != null) {
            firestore.collection("users").document(userId).collection("pieces").get()
                .addOnSuccessListener { pieceDocuments ->
                    var totalStockValue = 0.0
                    var totalAvailableStockValue = 0.0

                    val tasks = mutableListOf<Task<*>>()
                    for (pieceDoc in pieceDocuments) {
                        val pieceId = pieceDoc.id
                        val piecePrice = pieceDoc.get("price") as? Number ?: 0
                        val pieceQuantity = pieceDoc.getLong("quantity")?.toInt() ?: 0

                        val pieceTotalValue = piecePrice.toDouble() * pieceQuantity
                        totalStockValue += pieceTotalValue

                        val availableStockTask = firestore.collection("repairs")
                            .whereArrayContains("partsUsed", pieceId)
                            .get()
                            .addOnSuccessListener { repairDocs ->
                                val usedQuantity = repairDocs.size()
                                totalAvailableStockValue += piecePrice.toDouble() * (pieceQuantity - usedQuantity)
                            }
                        tasks.add(availableStockTask)
                    }

                    Tasks.whenAllSuccess<Void>(tasks).addOnCompleteListener {
                        valueStockArticle.text = formatCurrency(totalAvailableStockValue)
                        LinearProgressIndicator2.max = totalStockValue.toInt()
                        val progressPercentage = if (totalStockValue > 0) {
                            (totalAvailableStockValue / totalStockValue * 100).toInt()
                        } else {
                            0
                        }
                        LinearProgressIndicator2.progress = progressPercentage
                        LinearProgressIndicator2.setIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black_primary
                            )
                        )
                        LinearProgressIndicator2.trackColor =
                            ContextCompat.getColor(requireContext(), R.color.white)
                    }
                }.addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting pieces data: ", exception)
                }
        }

        return view
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchPieces() {

        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("pieces").get()
            .addOnSuccessListener { documents ->
                val pieces = mutableListOf<PieceData>()
                val tasks = mutableListOf<Task<*>>()

                for (document in documents) {
                    val pieceId = document.id
                    val name = document.getString("name") ?: "Unknown"
                    val quantity = document.getLong("quantity")?.toInt() ?: 0
                    val price = document.getDouble("price") as? Number ?: 0.0

                    val pieceData = PieceData(name, quantity, price, 0)
                    pieces.add(pieceData)

                    val repairTask = firestore.collection("repairs")
                        .whereArrayContains("partsUsed", pieceId)
                        .get()
                        .addOnSuccessListener { repairDocs ->
                            val usedQuantity = repairDocs.size()
                            val updatedPiece =
                                pieceData.copy(usedQuantity = usedQuantity, price = price)
                            pieces[pieces.indexOf(pieceData)] = updatedPiece
                            pieceAdapter.notifyDataSetChanged()
                        }
                    tasks.add(repairTask)
                }

                Tasks.whenAllSuccess<Void>(tasks).addOnCompleteListener {
                    pieceAdapter = PieceAdapter(pieces)
                    recyclerView.adapter = pieceAdapter
                }
            }.addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting pieces data: ", exception)
            }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    @SuppressLint("DefaultLocale")
    private fun formatCurrency(value: Double): String {
        return when {
            value >= 1_000_000_000 -> String.format("%.0f Md", value / 1_000_000_000)
            value >= 1_000_000 -> String.format("%.0f M", value / 1_000_000)
            value >= 1_000 -> String.format("%.0f K", value / 1_000)
            else -> String.format("%.0f", value)
        }
    }
}
