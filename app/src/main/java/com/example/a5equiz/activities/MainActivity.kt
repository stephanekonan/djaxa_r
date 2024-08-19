package com.example.a5equiz.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseActivity
import com.example.a5equiz.databinding.ActivityMainBinding
import com.example.a5equiz.fragments.EncoursFragment
import com.example.a5equiz.fragments.EnregistreFragment
import com.example.a5equiz.fragments.HomeFragment
import com.example.a5equiz.fragments.TermineFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val floatingactionbutton = findViewById<FloatingActionButton>(R.id.floatingactionbutton)

        val bottomNavigation = findViewById<AHBottomNavigation>(R.id.bottom_navigation)

        val item1 = AHBottomNavigationItem("Acceuil", R.drawable.ic_home)
        val item2 = AHBottomNavigationItem("Stock", R.drawable.ic_stock)
        val item3 = AHBottomNavigationItem("En cours", R.drawable.ic_encours)
        val item4 = AHBottomNavigationItem("Terminés", R.drawable.ic_termine)

        bottomNavigation.addItem(item1)
        bottomNavigation.addItem(item2)
        bottomNavigation.addItem(item3)
        bottomNavigation.addItem(item4)

        bottomNavigation.setAccentColor(resources.getColor(R.color.black_primary))
        bottomNavigation.setInactiveColor(resources.getColor(R.color.gray))
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }

        bottomNavigation.setOnTabSelectedListener { position, _ ->
            val selectedFragment: Fragment = when (position) {
                0 -> HomeFragment()
                1 -> EnregistreFragment()
                2 -> EncoursFragment()
                3 -> TermineFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment).commit()
            true
        }

        floatingactionbutton.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        setupEdgeToEdge(R.id.main)

    }
}