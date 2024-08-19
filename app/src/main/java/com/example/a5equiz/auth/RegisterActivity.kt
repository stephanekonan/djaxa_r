package com.example.a5equiz.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a5equiz.R
import com.example.a5equiz.activities.MainActivity
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnToLogin = findViewById<LinearLayout>(R.id.btnToLogin)
        val registerButton = findViewById<Button>(R.id.registerUserButton)

        setupBackButton(R.id.btnToBack)

        btnToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {

            val username = findViewById<EditText>(R.id.usernameInput).text.toString().trim()
            val phoneNumber = findViewById<EditText>(R.id.phoneNumberInput).text.toString().trim()
            val nameCompany = findViewById<EditText>(R.id.nameCompanyInput).text.toString().trim()
            val locationClient = findViewById<EditText>(R.id.locationClient).text.toString().trim()
            val email = findViewById<EditText>(R.id.emailInput).text.toString().trim()
            val password = findViewById<EditText>(R.id.passwordInput).text.toString().trim()

            if (validateInputs(
                    username,
                    phoneNumber,
                    email,
                    nameCompany,
                    password,
                    locationClient
                )
            ) {
                registerUser(username, phoneNumber, email, nameCompany, password, locationClient)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateInputs(
        username: String,
        phoneNumber: String,
        email: String,
        nameCompany: String,
        password: String,
        locationClient: String
    ): Boolean {
        if (username.length !in 5..25) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Nom complet, entre 5 et 25 carctères")
            return false
        }

        if (phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }) {
            showToast(
                ConstToast.TOAST_TYPE_ERROR,
                "Le numéro de téléphone doit contenir que 10 chiffres"
            )
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Veuillez entrer une adresse email valide")
            return false
        }

        if (nameCompany.length !in 5..35) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Nom du magasin, entre 5 et 35 carctères")
            return false
        }

        if (locationClient.length !in 5..55) {
            showToast(ConstToast.TOAST_TYPE_ERROR, "Adresse du magasin, entre 5 et 25 carctères")
            return false
        }

        if (password.length !in 5..10 || !password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{5,10}$".toRegex())) {
            showToast(
                ConstToast.TOAST_TYPE_ERROR,
                "Mot de passe, 5 à 10 caractères, inclure des lettres et des chiffres"
            )
            return false
        }

        return true
    }

    private fun registerUser(
        username: String,
        phoneNumber: String,
        email: String,
        nameCompany: String,
        password: String,
        locationClient: String
    ) {

        val registerButton = findViewById<Button>(R.id.registerUserButton)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)

        registerButton.isEnabled = false
        loadingProgressBar.visibility = android.view.View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->

            registerButton.isEnabled = true
            loadingProgressBar.visibility = android.view.View.GONE

            if (task.isSuccessful) {

                val user = auth.currentUser
                val userId = user?.uid

                val userData = hashMapOf(
                    "username" to username,
                    "phoneNumber" to phoneNumber,
                    "nameCompany" to nameCompany,
                    "locationClient" to locationClient,
                    "email" to email,
                    "role" to "client",
                    "createdAt" to FieldValue.serverTimestamp().toString()
                )

                if (userId != null) {
                    val recordRef = firestore.collection("users").document(userId).set(userData)
                    recordRef.addOnCompleteListener {
                        showToast(ConstToast.TOAST_TYPE_SUCCESS, "Vous êtes bien enregistrée")
                    }
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            } else {
                showToast(
                    ConstToast.TOAST_TYPE_ERROR,
                    "Vérifiez les données saisies ou votre connexion internet"
                )
            }
        }

    }


}