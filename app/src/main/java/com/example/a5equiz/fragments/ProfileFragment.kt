package com.example.a5equiz.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.a5equiz.R
import com.example.a5equiz.bases.BaseFragment
import com.example.a5equiz.config.ConstToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : BaseFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val cardView = view.findViewById<CardView>(R.id.cardView)
        cardView.setOnClickListener {
            expand(it)
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        user?.let {

            val username = view.findViewById<TextView>(R.id.username)
            val email = view.findViewById<TextView>(R.id.email)
            val phoneNumber = view.findViewById<TextView>(R.id.phoneNumber)
            val company = view.findViewById<TextView>(R.id.company)
            val location = view.findViewById<TextView>(R.id.location)

            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { dataSnapshot ->

                    val dbUsername = dataSnapshot.get("username") ?: "........."
                    val dbEmail = dataSnapshot.get("email") ?: "............."
                    val dbPhonenumber = dataSnapshot.get("phoneNumber") ?: "........."
                    val dbNamecompany = dataSnapshot.get("nameCompany") ?: ".........."
                    val dbLocationclient = dataSnapshot.get("locationClient") ?: "..........."

                    username.text = dbUsername.toString()
                    email.text = dbEmail.toString()
                    phoneNumber.text = dbPhonenumber.toString()
                    company.text = dbNamecompany.toString()
                    location.text = dbLocationclient.toString()

                }.addOnFailureListener {
                    showToast(ConstToast.TOAST_TYPE_ERROR, "Erreur de chargement du profil")
                }
        }

    }

    private fun expand(view: View) {

        val layoutBox = view.findViewById<LinearLayout>(R.id.layoutBox)
        val btnLayout = layoutBox.findViewById<LinearLayout>(R.id.infos)
        val v = if (btnLayout.visibility == View.GONE) View.VISIBLE else View.GONE

        val isVisible = btnLayout.visibility == View.GONE

        val translationY = if (isVisible) 0f else -btnLayout.height.toFloat()
        val alpha = if (isVisible) 1f else 0f

        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(btnLayout, "translationY", translationY),
                ObjectAnimator.ofFloat(btnLayout, "alpha", alpha)
            )
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        if (isVisible) {
            btnLayout.visibility = View.VISIBLE
            animatorSet.start()
        } else {
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    animation.let { super.onAnimationEnd(it) }
                    btnLayout.visibility = View.GONE
                }
            })
            animatorSet.start()
        }

    }

}