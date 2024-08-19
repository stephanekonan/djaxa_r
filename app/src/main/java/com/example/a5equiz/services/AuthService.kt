package com.example.a5equiz.services

import com.google.firebase.auth.FirebaseAuth

class AuthService {

    private lateinit var auth: FirebaseAuth

    fun register(email: String, passowrd: String) {
        auth.createUserWithEmailAndPassword(email, passowrd);
    }

    fun login(email: String, passowrd: String) {
        auth.signInWithEmailAndPassword(email, passowrd)
    }
}