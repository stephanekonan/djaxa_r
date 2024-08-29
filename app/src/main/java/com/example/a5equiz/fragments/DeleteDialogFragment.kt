package com.example.a5equiz.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.a5equiz.R
import com.example.a5equiz.activities.MainActivity
import com.example.a5equiz.bases.BaseBottomDialogFragment
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.databinding.FragmentDeleteDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment.STYLE_NORMAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class DeleteDialogFragment : BaseBottomDialogFragment() {

    private lateinit var binding: FragmentDeleteDialogBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeleteDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val repairId = arguments?.getString("REPAIR_ID") ?: return

        binding.confirmButton.setOnClickListener {
            deleteSingleRepairData(repairId)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

    private fun deleteSingleRepairData(repairId: String) {

        val actionsBtn = view?.findViewById<GridLayout>(R.id.actionsBtn)
        val loadingProgressBar = view?.findViewById<ProgressBar>(R.id.loadingProgressBar)

        actionsBtn?.visibility = View.GONE
        loadingProgressBar?.visibility = View.VISIBLE

        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("repairs")
            .document(repairId).delete()
            .addOnCompleteListener { task ->

                actionsBtn?.visibility = View.VISIBLE
                loadingProgressBar?.visibility = View.GONE

                if (task.isSuccessful) {
                    showToast(ConstToast.TOAST_TYPE_SUCCESS, "Client supprimé avec succès")
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    dismiss()
                } else {
                    showToast(
                        ConstToast.TOAST_TYPE_ERROR,
                        "Erreur lors de la suppression des données"
                    )
                }
            }
    }

}