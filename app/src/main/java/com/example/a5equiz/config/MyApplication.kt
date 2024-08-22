package com.example.a5equiz.config

import android.app.Application

class MyApplication : Application() {

    private var isUserConnected = false
    private var isCodeValidated = false

    fun onUserConnected() {
        isUserConnected = true
    }

    fun isUserConnected(): Boolean {
        return isUserConnected
    }

    fun onCodeValidated() {
        isCodeValidated = true
    }

    fun isCodeValidated(): Boolean {
        return isCodeValidated
    }

    fun resetCodeValidation() {
        isCodeValidated = false
    }
}
