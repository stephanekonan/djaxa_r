package com.example.a5equiz.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaos.view.PinView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a5equiz.R
import com.example.a5equiz.activities.MainActivity
import com.example.a5equiz.auth.LoginActivity
import com.example.a5equiz.config.ConstToast
import com.example.a5equiz.config.MyApplication

class LockActivity : AppCompatActivity() {

    private lateinit var pinView: PinView
    private lateinit var progressBar: ProgressBar
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        if (userId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        pinView = findViewById(R.id.firstPinView)
        progressBar = findViewById(R.id.loadingProgressBar)
        firestore = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.validateButton).setOnClickListener {
            val inputCode = pinView.text.toString()
            validateCode(inputCode)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateCode(inputCode: String) {
        if (inputCode.length != 4) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Veuillez entrer un code de 4 chiffres")
            return
        }

        val validateButton = findViewById<Button>(R.id.validateButton)
        val progressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)

        progressBar.visibility = View.VISIBLE
        validateButton.isEnabled = false
        validateButton.visibility = View.GONE

        val userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId!!)

        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val storedCode = document.getString("accessCode")

                if (storedCode == inputCode) {
                    showToast(ConstToast.TOAST_TYPE_SUCCESS, "Code correct")
                    progressBar.visibility = View.GONE
                    validateButton.isEnabled = true
                    validateButton.visibility = View.VISIBLE

                    (application as MyApplication).onCodeValidated()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    validateButton.isEnabled = true
                    validateButton.visibility = View.VISIBLE
                    showToast(ConstToast.TOAST_TYPE_ERROR, "Code incorrect")
                }
            } else {
                validateButton.isEnabled = true
                validateButton.visibility = View.VISIBLE
                showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur de connexion")
            }
            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur de connexion")
        }
    }

    fun showToast(toastType: Int, message: CharSequence) {
        val toastView = layoutInflater.inflate(
            R.layout.layout_toast,
            findViewById(R.id.layoutToastContainer)
        )

        val toastLayoutContainer = toastView.findViewById<LinearLayout>(R.id.layoutToastContainer)
        val imageIcon = toastView.findViewById<ImageView>(R.id.imageIcon)
        val textMessage = toastView.findViewById<TextView>(R.id.textMessage)

        when (toastType) {
            ConstToast.TOAST_TYPE_SUCCESS -> {
                toastLayoutContainer.setBackgroundResource(R.drawable.bg_toast_success)
                imageIcon.setImageResource(R.drawable.ic_check_circle)
                textMessage.text = message
            }

            ConstToast.TOAST_TYPE_ERROR -> {
                toastLayoutContainer.setBackgroundResource(R.drawable.bg_toast_error)
                imageIcon.setImageResource(R.drawable.ic_error)
                textMessage.text = message
            }

            ConstToast.TOAST_TYPE_WARNING -> {
                toastLayoutContainer.setBackgroundResource(R.drawable.bg_toast_warning)
                imageIcon.setImageResource(R.drawable.ic_warning)
                textMessage.text = message
            }
        }

        with(Toast(applicationContext)) {
            duration = Toast.LENGTH_SHORT
            view = toastView
            show()
        }
    }
}
