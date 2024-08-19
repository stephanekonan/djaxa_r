package com.example.a5equiz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a5equiz.activities.SplashActivity
import com.example.a5equiz.auth.LoginActivity

class StarterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val isFirstLaunch: Boolean = sharedPreferences.getBoolean("isFirstLaunch", true)

        val intent: Intent = if (isFirstLaunch) {
            Intent(this, SplashActivity::class.java).also {
                sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
            }
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}