package com.example.a5equiz.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseBottomDialogFragment
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.databinding.FragmentRegistreClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistreClientFragment : BaseBottomDialogFragment() {

    private lateinit var binding: FragmentRegistreClientBinding
    private lateinit var fullNameInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var phoneMarqueInput: EditText
    private lateinit var numeroSeriePhone: EditText
    private lateinit var issueInput: EditText
    private lateinit var montantNormal: EditText
    private lateinit var montantNegocie: EditText
    private lateinit var dateInput: EditText
    private lateinit var pieceId: Spinner
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var pieceIds = mutableListOf<String>()
    private var pieceNames = mutableListOf<String>()
    private var piecePrices = mutableListOf<Any>()
    private var selectedPieceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistreClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        fullNameInput = view.findViewById(R.id.fullName)
        phoneNumberInput = view.findViewById(R.id.phoneNumberInput)
        phoneMarqueInput = view.findViewById(R.id.phoneMarqueInput)
        issueInput = view.findViewById(R.id.issueInput)
        numeroSeriePhone = view.findViewById(R.id.numeroSeriePhone)
        montantNormal = view.findViewById(R.id.montantNormal)
        montantNegocie = view.findViewById(R.id.montantNegocie)
        dateInput = view.findViewById(R.id.dateInput)
        pieceId = view.findViewById(R.id.pieceId)

        dateInput.inputType = InputType.TYPE_NULL
        dateInput.setOnClickListener {
            showDateTimePickerDialog()
        }

        val registerButton = view.findViewById<TextView>(R.id.saveRecordBtn)
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

                montantNormal.setText(piecePrice.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPieceId = null
                montantNormal.text = null
            }
        }

        fetchPieces()

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
                    requireContext(),
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

    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
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
            requireContext(),
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

    private fun saveRepairToDatabase() {
        val userId = mAuth.currentUser?.uid ?: return
        val pieceId = selectedPieceId ?: return
        val customerName = fullNameInput.text.toString().trim()
        val customerPhone = phoneNumberInput.text.toString().trim()
        val marquePhone = phoneMarqueInput.text.toString().trim()
        val issuePhone = issueInput.text.toString().trim()
        val numeroSeriePhone = numeroSeriePhone.text.toString().trim()
        val montantNormalPiece = montantNormal.text.toString().trim()
        val montantNegociePiece = montantNegocie.text.toString().trim()
        val date = dateInput.text.toString().trim()
        val status = 1
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
            "numeroSeriePhone" to numeroSeriePhone,
            "montantNormalPiece" to montantNormalPiece,
            "montantNegociePiece" to montantNegociePiece,
            "dateDeposit" to date,
            "status" to status,
            "createdAt" to createdAtString
        )

        saveRepairs(repairRef, repair) { success ->
            if (success) {
                dismiss()
            }
        }
    }

    private fun saveRepairs(
        repairRef: DocumentReference,
        repair: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        val registerButton = view?.findViewById<TextView>(R.id.saveRecordBtn)
        val loadingProgressBar = view?.findViewById<ProgressBar>(R.id.loadingProgressBar)

        registerButton?.isEnabled = false
        loadingProgressBar?.visibility = View.VISIBLE

        repairRef.set(repair).addOnCompleteListener { task ->
            registerButton?.isEnabled = true
            loadingProgressBar?.visibility = View.GONE

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


}