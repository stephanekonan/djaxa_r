package com.example.a5equiz.models

data class Repair(
    val repairId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val dateDeposit: String = "",
    val descriptionRepair: String = "",
    val issuePhone: String = "",
    val marquePhone: String = "",
    val montantNegociePiece: String = "",
    val montantNormalPiece: String = "",
    val pieceId: String = "",
    val status: Int = 0,
    var createdAt: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", 0, "")
}
