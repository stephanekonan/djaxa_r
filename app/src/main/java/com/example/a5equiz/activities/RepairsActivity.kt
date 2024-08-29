package com.example.a5equiz.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R
import com.example.a5equiz.adapters.RepairSearchAdapter
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.models.Repair
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RepairsActivity : BaseActivity() {

    private lateinit var repairsAdapter: RepairSearchAdapter
    private lateinit var repairsList: MutableList<Repair>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repairs)

        val repairsRecyclerView = findViewById<RecyclerView>(R.id.repairsRecyclerView)
        repairsList = mutableListOf()
        repairsAdapter = RepairSearchAdapter(this, repairsList)
        repairsRecyclerView.layoutManager = LinearLayoutManager(this)
        repairsRecyclerView.adapter = repairsAdapter

        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    repairsAdapter.filter(s.toString())
                    toggleNoDataView(repairsAdapter.itemCount)
                }
            }
        )

        fetchRepairs()

        val btnToBack: ImageButton = findViewById(R.id.btnToBack)

        btnToBack.setOnClickListener {
            finish()
        }

        setupEdgeToEdge(R.id.main)
    }

    private fun fetchRepairs() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val repairsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("repairs")

        repairsRef.get().addOnSuccessListener { querySnapshot ->
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
        val noFoundLayoud = findViewById<LinearLayout>(R.id.noFoundLayoud)
        val repairsRecyclerView = findViewById<RecyclerView>(R.id.repairsRecyclerView)

        if (itemCount == 0) {
            noFoundLayoud?.visibility = View.VISIBLE
            repairsRecyclerView?.visibility = View.GONE
        } else {
            noFoundLayoud?.visibility = View.GONE
            repairsRecyclerView?.visibility = View.VISIBLE
        }
    }

}
