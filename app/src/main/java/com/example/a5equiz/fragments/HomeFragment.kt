package com.example.a5equiz.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.adapters.RepairAdapter
import com.example.a5equiz.auth.LoginActivity
import com.example.a5equiz.bases.BaseFragment
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.models.Repair
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repairsRecyclerView: RecyclerView
    private lateinit var repairsAdapter: RepairAdapter
    private val repairs = mutableListOf<Repair>()
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noFoundDataLayout: LinearLayout
    private lateinit var totalAmountTextView: TextView
    private lateinit var totalCountTextView: TextView
    private lateinit var doneCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repairsRecyclerView = view.findViewById(R.id.repairsRecyclerView)
        repairsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        repairsAdapter = RepairAdapter(requireContext(), repairs)
        repairsRecyclerView.adapter = repairsAdapter
        shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        noFoundDataLayout = view.findViewById<LinearLayout>(R.id.noFoundDataLayout)

        totalAmountTextView = view.findViewById(R.id.totalAmountTextView)
        totalCountTextView = view.findViewById(R.id.totalCountTextView)
        doneCountTextView = view.findViewById(R.id.doneCountTextView)

        val logoutButton = view.findViewById<ImageButton>(R.id.logoutButton)

        logoutButton.setOnClickListener {

            auth.signOut()
            showToast(ConstToast.TOAST_TYPE_SUCCESS, "Déconnecté avec succès")

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        fetchCustomers()
        fetchTodayTotalAmount()
        fetchTodayTotalCount()
        fetchTodayRepairsDoneCount()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCustomers() {
        val userId = auth.currentUser?.uid ?: return
        val repairsRef = firestore.collection("users").document(userId).collection("repairs")

        repairsRef.addSnapshotListener { snapshot, error ->
            if (!isAdded) return@addSnapshotListener

            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur lors de la récupération des données")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                repairs.clear()
                for (document in snapshot.documents) {
                    val repair = document.toObject(Repair::class.java)
                    if (repair != null) {
                        repairs.add(repair)
                    }
                }
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                noFoundDataLayout.visibility = View.GONE
            } else {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                noFoundDataLayout.visibility = View.VISIBLE
            }

            repairsAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun fetchTodayTotalAmount() {
        val userId = auth.currentUser?.uid ?: return
        val repairsRef = firestore.collection("users").document(userId).collection("repairs")

        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        repairsRef
            .whereEqualTo("status", "termine")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    showToast(
                        ConstToast.TOAST_TYPE_ERROR,
                        "Erreur lors de la récupération des données"
                    )
                    return@addSnapshotListener
                }
                var totalAmount = 0

                if (snapshot != null && !snapshot.isEmpty) {

                    for (document in snapshot.documents) {
                        val createdAtString = document.getString("createdAt") ?: ""
                        if (createdAtString.startsWith(todayDateString)) {
                            val montantNegociePiece = document.get("montantNegociePiece")

                            if (montantNegociePiece is Number) {
                                totalAmount += montantNegociePiece.toInt()
                            } else if (montantNegociePiece is String) {
                                montantNegociePiece.toIntOrNull()?.let {
                                    totalAmount += it
                                }
                            }
                        }
                    }

                    totalAmountTextView.text = formatAmount(totalAmount)
                } else {
                    totalAmountTextView.text = formatAmount(totalAmount)
                }
            }
    }

    @SuppressLint("DefaultLocale")
    private fun formatAmount(amount: Int): String {
        return when {
            amount >= 1_000_000_000 -> String.format("%.1fMd", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
            amount >= 1_000 -> String.format("%.1fK", amount / 1_000.0)
            else -> amount.toString()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun fetchTodayTotalCount() {
        val userId = auth.currentUser?.uid ?: return
        val repairsRef = firestore.collection("users").document(userId).collection("repairs")

        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        repairsRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    showToast(
                        ConstToast.TOAST_TYPE_ERROR,
                        "Erreur lors de la récupération des données"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val todayRepairsCount = snapshot.documents.count { document ->
                        val createdAtString = document.getString("createdAt") ?: ""
                        createdAtString.startsWith(todayDateString)
                    }

                    totalCountTextView.text = "$todayRepairsCount"
                } else {
                    totalCountTextView.text = "0.0"
                }
            }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun fetchTodayRepairsDoneCount() {
        val userId = auth.currentUser?.uid ?: return
        val repairsRef = firestore.collection("users").document(userId).collection("repairs")

        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        repairsRef
            .whereEqualTo("status", "termine")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    showToast(
                        ConstToast.TOAST_TYPE_ERROR,
                        "Erreur lors de la récupération des données"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val todayRepairsCount = snapshot.documents.count { document ->
                        val createdAtString = document.getString("createdAt") ?: ""
                        createdAtString.startsWith(todayDateString)
                    }

                    doneCountTextView.text = "$todayRepairsCount"
                } else {
                    doneCountTextView.text = "0.0"
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }
}