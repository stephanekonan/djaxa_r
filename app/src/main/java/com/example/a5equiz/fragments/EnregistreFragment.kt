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
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.models.PieceData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class EnregistreFragment : BaseFragment(), AddPieceFragment.OnPieceAddedListener {

    override fun onPieceAdded() {
        fetchPieces()
    }
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var pieceAdapter: PieceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesContainer: LinearLayout = view.findViewById(R.id.categories_container)
        val addNewPieceBtn: Button = view.findViewById(R.id.addNewPieceBtn)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        pieceAdapter = PieceAdapter(emptyList())
        recyclerView.adapter = pieceAdapter

        addNewPieceBtn.setOnClickListener {
            val recordFragment = AddPieceFragment()
            recordFragment.show(parentFragmentManager, recordFragment.tag)
        }

        fetchCategories(categoriesContainer)
        fetchPieces()
        calculateTotalStockValue { totalValue ->
            if (isAdded) {
                val totalValueTextView = view.findViewById<TextView>(R.id.valueStock)
                totalValueTextView.text = formatCurrency(totalValue)
            }
        }
        calculateTotalPieceCount { totalCountValue ->
            if (isAdded) {
                val totalValueTextView = view.findViewById<TextView>(R.id.valueStockArticle)
                totalValueTextView.text = formatCurrency(totalCountValue)
            }
        }
    }

    private fun fetchCategories(container: LinearLayout) {
        firestore.collection("categories").get().addOnSuccessListener { documents ->
            if (isAdded) {
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

                    container.addView(categoryTextView)
                }
            }
        }.addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting documents: ", exception)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchPieces() {
        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("pieces").get()
            .addOnSuccessListener { documents ->
                val pieces = mutableListOf<PieceData>()
                val tasks = mutableListOf<Task<QuerySnapshot>>()

                for (document in documents) {
                    val pieceId = document.id
                    val name = document.getString("name") ?: "Unknown"
                    val quantity = document.getLong("quantity")?.toInt() ?: 0
                    val price = document.getDouble("price") ?: 0.0
                    val createdAt = document.getString("createdAt") ?: "Unknown"

                    val pieceData = PieceData(name, quantity, price, 0, createdAt)
                    pieces.add(pieceData)

                    val repairTask2 = firestore.collection("users")
                        .document(userId)
                        .collection("repairs")
                        .whereEqualTo("pieceId", pieceId)
                        .whereEqualTo("status", 2)
                        .get()

                    val repairTask3 = firestore.collection("users")
                        .document(userId)
                        .collection("repairs")
                        .whereEqualTo("pieceId", pieceId)
                        .whereEqualTo("status", 3)
                        .get()

                    tasks.add(repairTask2)
                    tasks.add(repairTask3)

                    Tasks.whenAllComplete(repairTask2, repairTask3).addOnSuccessListener {
                        if (isAdded) {
                            val usedQuantity2 = repairTask2.result?.size() ?: 0
                            val usedQuantity3 = repairTask3.result?.size() ?: 0
                            val totalUsedQuantity = usedQuantity2 + usedQuantity3

                            val updatedPiece = pieceData.copy(usedQuantity = totalUsedQuantity)
                            val index = pieces.indexOfFirst { it.name == pieceData.name }
                            if (index != -1) {
                                pieces[index] = updatedPiece
                            }
                        }
                    }
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    if (isAdded) {
                        pieceAdapter.updateData(pieces)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting pieces data: ", exception)
            }
    }

    private fun calculateTotalStockValue(onComplete: (Double) -> Unit) {
        val userId = mAuth.currentUser?.uid ?: return
        val piecesRef = firestore.collection("users").document(userId).collection("pieces")

        piecesRef.get().addOnSuccessListener { querySnapshot ->
            var totalValue = 0.0

            for (document in querySnapshot.documents) {
                val quantity = document.getLong("quantity") ?: 0
                val price = document.getDouble("price") ?: 0.0
                totalValue += quantity * price
            }

            onComplete(totalValue)
        }.addOnFailureListener { e ->
            showToast(
                ConstToast.TOAST_TYPE_ERROR,
                "Erreur lors du calcul de la valeur totale des pièces"
            )
            e.printStackTrace()
            onComplete(0.0)
        }
    }

    private fun calculateTotalPieceCount(onComplete: (Double) -> Unit) {
        val userId = mAuth.currentUser?.uid ?: return
        val piecesRef = firestore.collection("users").document(userId).collection("pieces")

        piecesRef.get().addOnSuccessListener { querySnapshot ->
            var totalPieces = 0.0

            for (document in querySnapshot.documents) {
                val quantity = document.getLong("quantity") ?: 0
                totalPieces += quantity
            }

            onComplete(totalPieces)
        }.addOnFailureListener { e ->
            showToast(
                ConstToast.TOAST_TYPE_ERROR,
                "Erreur lors du calcul du nombre total de pièces en stock"
            )
            e.printStackTrace()
            onComplete(0.0)
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

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enregistre, container, false)
    }
}

