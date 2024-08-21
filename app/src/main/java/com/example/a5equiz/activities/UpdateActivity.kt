package com.example.a5equiz.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import com.example.a5equiz.R
import com.example.a5equiz.auth.RegisterActivity
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class UpdateActivity : BaseActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val statusMap = mapOf(
        "Enregistré" to 1,
        "En cours" to 2,
        "Terminé" to 3
    )

    private val reverseStatusMap = mapOf(
        1 to "Enregistré",
        2 to "En cours",
        3 to "Terminé"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val repairId = intent.getStringExtra("REPAIR_ID")
        val pieceId = intent.getStringExtra("PIECE_ID")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")
        val customerPhone = intent.getStringExtra("CUSTOMER_PHONE")
        val descriptionRepair = intent.getStringExtra("DESCRIPTION_REPAIR")
        val montantNegocie = intent.getStringExtra("MONTANT_NEGOCIE")
        val status = intent.getIntExtra("STATUS", 0)

        val customerNameEditText: EditText = findViewById(R.id.customerName)
        val customerPhoneEditText: EditText = findViewById(R.id.customerPhoneNumber)
        val descriptionRepairEditText: EditText = findViewById(R.id.descriptionRepair)
        val montantNegocieEditText: EditText = findViewById(R.id.montantNegocie)
        val statusSpinner: Spinner = findViewById(R.id.statusSpinner)

        customerNameEditText.setText(customerName)
        customerPhoneEditText.setText(customerPhone)
        descriptionRepairEditText.setText(descriptionRepair)
        montantNegocieEditText.setText(montantNegocie)

        val statusOptions = if (status == 2) {
            arrayOf("En cours", "Terminé")
        } else {
            arrayOf("Enregistré", "En cours", "Terminé")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = adapter

        status.let {
            val statusKey = reverseStatusMap[it]
            statusKey?.let { key ->
                val position = statusOptions.indexOf(key)
                statusSpinner.setSelection(position)
            }
        }

        val updateBtn: Button = findViewById(R.id.updateButton)

        updateBtn.setOnClickListener {
            repairId?.let { id ->
                val selectedStatus = statusMap[statusSpinner.selectedItem.toString()] ?: 1

                val updatedRepair = mapOf(
                    "customerName" to customerNameEditText.text.toString(),
                    "customerPhone" to customerPhoneEditText.text.toString(),
                    "descriptionRepair" to descriptionRepairEditText.text.toString(),
                    "montantNegociePiece" to montantNegocieEditText.text.toString(),
                    "status" to selectedStatus
                )

                updateRepair(id, updatedRepair, pieceId) { success ->
                    if (success) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        setupBackButton(R.id.btnToBack)

        setupEdgeToEdge(R.id.main)
    }

    private fun updateRepair(
        repairId: String,
        repair: Map<String, Any>,
        pieceId: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val updateButton = findViewById<Button>(R.id.updateButton)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)

        updateButton.isEnabled = true
        updateButton.visibility = View.GONE
        loadingProgressBar.visibility = View.VISIBLE

        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("repairs")
            .document(repairId).update(repair)
            .addOnCompleteListener { task ->

                updateButton.isEnabled = true
                updateButton.visibility = View.VISIBLE
                loadingProgressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    if (repair["status"] == 2) {
                        pieceId?.let { updatePieceQuantity(it) }
                    }
                    showToast(
                        ConstToast.TOAST_TYPE_SUCCESS,
                        "Réparation mise à jour avec succès"
                    )
                    onComplete(true)
                } else {
                    showToast(
                        ConstToast.TOAST_TYPE_ERROR,
                        "Erreur lors de la mise à jour des données"
                    )
                    onComplete(false)
                }
            }
    }

    private fun updatePieceQuantity(pieceId: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val pieceRef =
            firestore.collection("users").document(userId).collection("pieces").document(pieceId)

        firestore.runTransaction { transaction ->
            val pieceSnapshot = transaction.get(pieceRef)
            val currentQuantity = pieceSnapshot.getLong("quantity") ?: 0
            if (currentQuantity > 0) {
                transaction.update(pieceRef, "quantity", currentQuantity - 1)
            } else {
                throw FirebaseFirestoreException(
                    "Stock épuisé",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }
        }.addOnSuccessListener {
            showToast(ConstToast.TOAST_TYPE_SUCCESS, "Stock mis à jour")
        }.addOnFailureListener { e ->
            showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur lors de la mise à jour du stock")
            e.printStackTrace()
        }
    }
}