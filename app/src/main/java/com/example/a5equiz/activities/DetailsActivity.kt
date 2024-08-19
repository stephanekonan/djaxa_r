package com.example.a5equiz.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseActivity

class DetailsActivity : BaseActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        val status: TextView = findViewById(R.id.status)
        val customerName: TextView = findViewById(R.id.customerName)
        val phoneCustomer: TextView = findViewById(R.id.phoneCustomer)
        val phoneModel: TextView = findViewById(R.id.phoneModel)
        val issue: TextView = findViewById(R.id.issue)
        val description: TextView = findViewById(R.id.description)
        val repairDate: TextView = findViewById(R.id.repairDate)
        val montantNormal: TextView = findViewById(R.id.montantNormal)
        val montantNegocie:  TextView = findViewById(R.id.montantNegocie)
        val deleteBtn: TextView = findViewById(R.id.deleteBtn)

        status.text = intent.getStringExtra("status")
        customerName.text = intent.getStringExtra("customerName")
        phoneCustomer.text = intent.getStringExtra("phoneCustomer")
        description.text = intent.getStringExtra("description")
        issue.text = intent.getStringExtra("issue")
        phoneModel.text = intent.getStringExtra("phoneModel")
        repairDate.text = intent.getStringExtra("repairDate")
        montantNormal.text = intent.getStringExtra("montantNormal")
        montantNegocie.text = intent.getStringExtra("montantNegocie")

        deleteBtn.setOnClickListener {
            finish()
        }

        setupEdgeToEdge(R.id.main)
    }
}