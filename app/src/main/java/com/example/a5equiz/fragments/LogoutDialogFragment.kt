package com.example.a5equiz.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.a5equiz.R
import com.example.a5equiz.auth.LoginActivity
import com.example.a5equiz.bases.BaseBottomDialogFragment
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth

class LogoutDialogFragment : BaseBottomDialogFragment() {

    private lateinit var mAuth: FirebaseAuth

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_logout_confirmation, container, false)
 
        mAuth = FirebaseAuth.getInstance()

        val confirmButton = view.findViewById<TextView>(R.id.confirmButton)
        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            mAuth.signOut()
            showToast(ConstToast.TOAST_TYPE_SUCCESS, "Déconnecté avec succès")

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}