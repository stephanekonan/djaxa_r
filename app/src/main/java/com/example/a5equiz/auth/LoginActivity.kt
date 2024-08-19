package com.example.a5equiz.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a5equiz.R
import com.example.a5equiz.activities.MainActivity
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnToRegister = findViewById<TextView>(R.id.btnToRegister)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)

        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()
            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Veuillez entrer une adresse email valide")
            return false
        }

        if (password.isEmpty()) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Le mot de passe ne peut pas être vide")
            return false
        }

        return true
    }

    private fun loginUser(email: String, password: String) {

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)

        loginBtn.isEnabled = false
        loadingProgressBar.visibility = android.view.View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                loginBtn.isEnabled = true
                loadingProgressBar.visibility = android.view.View.GONE

                if (task.isSuccessful) {
                    showToast(ConstToast.TOAST_TYPE_SUCCESS, "Vous êtes connectés")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val exceptionMessage = task.exception?.message
                    if (exceptionMessage != null && exceptionMessage.contains("The password is invalid")) {
                        showToast(ConstToast.TOAST_TYPE_ERROR, "Mot de passe incorrect")
                    } else {
                        showToast(
                            ConstToast.TOAST_TYPE_ERROR,
                            "Vérifiez vos données saisies ou votre connexion"
                        )
                    }
                }
            }
    }
}