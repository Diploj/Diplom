package com.example.faceattend.api.response

data class LectorProfile(
    val userId: Int,
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val department: String?
)