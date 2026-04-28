package com.example.faceattend.api.response

data class UserProfile(
    val userId: Int,
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val role: String,
    val studentIdNumber: String? = null,
    val groupId: Int? = null,
    val department: String? = null
)