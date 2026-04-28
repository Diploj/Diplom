package com.example.faceattend.api.response

data class LoginResponse(
    val id: Int,
    val token: String,
    val role: String,
    val expiration: String
)