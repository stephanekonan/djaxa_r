package com.example.a5equiz.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateActivity : BaseActivity() {

    private lateinit var fullNameInput: EditText
    private lateinit var phoneMarqueInput: EditText
    private lateinit var phoneNumberInput: TextView
    private lateinit var descriptionInput: EditText
    private lateinit var issueInput: EditText
    private lateinit var montantNormal: EditText
    private lateinit var montantNegocie: EditText
    private lateinit var dateInput: EditText
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var pieceId: Spinner
    private var pieceIds = mutableListOf<String>()
    private var pieceNames = mutableListOf<String>()
    private var piecePrices = mutableListOf<Any>()
    private var selectedPieceId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        database = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        fullNameInput = findViewById(R.id.fullName)
        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        phoneMarqueInput = findViewById(R.id.phoneMarqueInput)
        issueInput = findViewById(R.id.issueInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        montantNormal = findViewById(R.id.montantNormal)
        montantNegocie = findViewById(R.id.montantNegocie)
        dateInput = findViewById(R.id.dateInput)
        pieceId = findViewById(R.id.pieceId)

        dateInput.inputType = InputType.TYPE_NULL
        dateInput.setOnClickListener {
            showDateTimePickerDialog()
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            saveRepairToDatabase()
        }

        pieceId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPieceId = pieceIds[position]
                val piecePrice = piecePrices[position]

                findViewById<EditText>(R.id.montantNormal).setText(piecePrice.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPieceId = null
                findViewById<EditText>(R.id.montantNormal).text = null
            }
        }

        fetchPieces()

        setupBackButton(R.id.btnToBack)
        setupEdgeToEdge(R.id.main)
    }

    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                showTimePickerDialog(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(selectedDate: Calendar) {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedDate.set(Calendar.MINUTE, selectedMinute)

                val selectedDateTime = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault()
                ).format(selectedDate.time)

                dateInput.setText(selectedDateTime)
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }

    private fun fetchPieces() {

        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("pieces").get()
            .addOnSuccessListener { documents ->
                pieceIds.clear()
                pieceNames.clear()
                piecePrices.clear()

                for (document in documents) {
                    val pieceName = document.getString("name") ?: "Unknown"
                    val pieceId = document.id
                    val piecePrice = document.get("price")?.let {
                        (it as? Number)?.toInt() ?: 0
                    } ?: 0

                    pieceNames.add(pieceName)
                    pieceIds.add(pieceId)
                    piecePrices.add(piecePrice)
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    pieceNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                pieceId.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting pieces: ", exception)
            }
    }

    private fun saveRepairToDatabase() {
        val userId = mAuth.currentUser?.uid ?: return
        val pieceId = selectedPieceId ?: return
        val customerName = fullNameInput.text.toString().trim()
        val customerPhone = phoneNumberInput.text.toString().trim()
        val marquePhone = phoneMarqueInput.text.toString().trim()
        val issuePhone = issueInput.text.toString().trim()
        val descriptionRepair = descriptionInput.text.toString().trim()
        val montantNormalPiece = montantNormal.text.toString().trim()
        val montantNegociePiece = montantNegocie.text.toString().trim()
        val date = dateInput.text.toString().trim()
        val status = "enregistre"
        val createdAtString =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val repairRef =
            firestore.collection("users").document(userId).collection("repairs").document()

        val repair = hashMapOf(
            "repairId" to repairRef.id,
            "pieceId" to pieceId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "marquePhone" to marquePhone,
            "issuePhone" to issuePhone,
            "descriptionRepair" to descriptionRepair,
            "montantNormalPiece" to montantNormalPiece,
            "montantNegociePiece" to montantNegociePiece,
            "dateDeposit" to date,
            "status" to status,
            "createdAt" to createdAtString
        )

        saveRepairs(repairRef, repair) { success ->
            if (success) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun saveRepairs(
        repairRef: DocumentReference,
        repair: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)

        registerButton.isEnabled = false
        loadingProgressBar.visibility = View.VISIBLE

        repairRef.set(repair).addOnCompleteListener { task ->
            registerButton.isEnabled = true
            loadingProgressBar.visibility = View.GONE

            if (task.isSuccessful) {
                showToast(ConstToast.TOAST_TYPE_SUCCESS, "Client enregistré avec succès")
                onComplete(true)
            } else {
                showToast(
                    ConstToast.TOAST_TYPE_ERROR,
                    "Erreur lors de l'enregistrement des données"
                )
                onComplete(false)
            }
        }
    }

}