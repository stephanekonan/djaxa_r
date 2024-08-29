package com.example.a5equiz.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.fragments.DeleteDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : BaseActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val deleteBtn: TextView = findViewById(R.id.deleteBtn)
        val editButton: Button = findViewById(R.id.editButton)
        val btnToBack: ImageButton = findViewById(R.id.btnToBack)

        val repairId = intent.getStringExtra("REPAIR_ID")
        val pieceId = intent.getStringExtra("PIECE_ID")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")
        val customerPhone = intent.getStringExtra("CUSTOMER_PHONE")
        val dateDeposit = intent.getStringExtra("DATE_DEPOSIT")
        val numeroSeriePhone = intent.getStringExtra("NUMERO_SERIE_PHONE")
        val issuePhone = intent.getStringExtra("ISSUE_PHONE")
        val marquePhone = intent.getStringExtra("MARQUE_PHONE")
        val montantNegocie = intent.getStringExtra("MONTANT_NEGOCIE")
        val montantNormal = intent.getStringExtra("MONTANT_NORMAL")
        val status = intent.getIntExtra("STATUS", 0)

        val statusText = when (status) {
            1 -> "enregistre"
            2 -> "en cours"
            3 -> "termine"
            else -> "inconnu"
        }

        findViewById<TextView>(R.id.repairId).text = repairId
        findViewById<TextView>(R.id.customerName).text = customerName
        findViewById<TextView>(R.id.customerPhone).text = customerPhone
        findViewById<TextView>(R.id.dateDeposit).text = dateDeposit
        findViewById<TextView>(R.id.numeroSeriePhone).text = numeroSeriePhone
        findViewById<TextView>(R.id.issuePhone).text = issuePhone
        findViewById<TextView>(R.id.marquePhone).text = marquePhone
        findViewById<TextView>(R.id.montantNegocie).text = montantNegocie.plus(" F CFA")
        findViewById<TextView>(R.id.montantNormal).text = montantNormal.plus(" F CFA")
        findViewById<TextView>(R.id.status).text = statusText

        if (status == 2) {
            deleteBtn.isEnabled = false
            deleteBtn.visibility = View.GONE
        }

        if (status == 3) {
            deleteBtn.isEnabled = false
            deleteBtn.visibility = View.GONE
            editButton.isEnabled = false
            editButton.visibility = View.GONE
        }

        deleteBtn.setOnClickListener {
            val bottomSheetDialog = DeleteDialogFragment()

            val bundle = Bundle()
            bundle.putString("REPAIR_ID", repairId)
            bottomSheetDialog.arguments = bundle

            bottomSheetDialog.show(supportFragmentManager, "DeleteBottomSheetDialog")
        }

        btnToBack.setOnClickListener {
            finish()
        }

        editButton.setOnClickListener {
            repairId?.let { id ->
                val intent = Intent(this, UpdateActivity::class.java).apply {
                    putExtra("REPAIR_ID", id)
                    putExtra("PIECE_ID", pieceId)
                    putExtra("CUSTOMER_NAME", customerName)
                    putExtra("CUSTOMER_PHONE", customerPhone)
                    putExtra("DATE_DEPOSIT", dateDeposit)
                    putExtra("NUMERO_SERIE_PHONE", numeroSeriePhone)
                    putExtra("ISSUE_PHONE", issuePhone)
                    putExtra("MARQUE_PHONE", marquePhone)
                    putExtra("MONTANT_NEGOCIE", montantNegocie)
                    putExtra("MONTANT_NORMAL", montantNormal)
                    putExtra("STATUS", status)
                }
                startActivity(intent)
            }
        }

        setupEdgeToEdge(R.id.main)
    }

}