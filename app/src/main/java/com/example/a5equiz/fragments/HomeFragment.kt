package com.example.a5equiz.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.adapters.CustomerAdapter
import com.example.a5equiz.auth.LoginActivity
import com.example.a5equiz.bases.BaseFragment
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.models.Customer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var customersRecyclerView: RecyclerView
    private lateinit var customerAdapter: CustomerAdapter
    private val customers = mutableListOf<Customer>()
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noFoundDataLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customersRecyclerView = view.findViewById(R.id.customersRecyclerView)
        customersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        customerAdapter = CustomerAdapter(requireContext(), customers)
        customersRecyclerView.adapter = customerAdapter
        shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        noFoundDataLayout = view.findViewById<LinearLayout>(R.id.noFoundDataLayout)

        val logoutButton = view.findViewById<ImageButton>(R.id.logoutButton)

        logoutButton.setOnClickListener {

            auth.signOut()
            showToast(ConstToast.TOAST_TYPE_SUCCESS, "Déconnecté avec succès")

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        fetchCustomers()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCustomers() {
        val userId = auth.currentUser?.uid ?: return
        val customersRef = firestore.collection("users").document(userId).collection("repairs")

        customersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur lors de la récupération des données")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                customers.clear()
                for (document in snapshot.documents) {
                    val customer = document.toObject(Customer::class.java)
                    if (customer != null) {
                        customers.add(customer)
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

            customerAdapter.notifyDataSetChanged()
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