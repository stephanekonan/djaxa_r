package com.example.a5equiz.models

data class Repair(
    val customerName: String = "",
    val phoneCustomer: String = "",
    val phoneModel: String = "",
    val issue: String = "",
    val description: String = "",
    val status: String = "enregistre",
    val montantNormal: String,
    val montantNegocie: String,
    val repairDate: String = ""
)
