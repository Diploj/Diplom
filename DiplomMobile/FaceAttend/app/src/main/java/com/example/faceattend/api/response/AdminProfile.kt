package com.example.faceattend.api.response

data class AdminProfile(
    val userId: Int,
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
)