package com.example.a5equiz.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseActivity

class UpdateActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update)

        setupBackButton(R.id.btnToBack)

        setupEdgeToEdge(R.id.main)
    }
}