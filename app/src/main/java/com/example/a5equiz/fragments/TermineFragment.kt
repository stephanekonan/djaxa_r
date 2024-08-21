package com.example.a5equiz.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.adapters.RepairSearchAdapter
import com.example.a5equiz.bases.BaseFragment
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.models.Repair
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TermineFragment : BaseFragment() {

    private lateinit var repairsAdapter: RepairSearchAdapter
    private lateinit var repairsList: MutableList<Repair>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repairsRecyclerView = view.findViewById<RecyclerView>(R.id.repairsRecyclerView)
        repairsList = mutableListOf()
        repairsAdapter = RepairSearchAdapter(requireContext(), repairsList)
        repairsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        repairsRecyclerView.adapter = repairsAdapter

        fetchRepairsWithStatus2()

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                repairsAdapter.filter(s.toString())
                toggleNoDataView(repairsAdapter.itemCount)
            }
        })
    }

    private fun fetchRepairsWithStatus2() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val repairsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("repairs")

        repairsRef.whereEqualTo("status", 3)
            .get()
            .addOnSuccessListener { querySnapshot ->
                repairsList = querySnapshot.toObjects(Repair::class.java)
                repairsAdapter.updateList(repairsList)
                toggleNoDataView(repairsAdapter.itemCount)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Error", e.message.toString())
                showToast(
                    ConstToast.TOAST_TYPE_ERROR,
                    "Erreur lors de la récupération des réparations"
                )
            }
    }

    private fun toggleNoDataView(itemCount: Int) {
        val noFoundLayoud = view?.findViewById<LinearLayout>(R.id.noFoundLayoud)
        val repairsRecyclerView = view?.findViewById<RecyclerView>(R.id.repairsRecyclerView)

        if (itemCount == 0) {
            noFoundLayoud?.visibility = View.VISIBLE
            repairsRecyclerView?.visibility = View.GONE
        } else {
            noFoundLayoud?.visibility = View.GONE
            repairsRecyclerView?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_termine, container, false)
    }
}