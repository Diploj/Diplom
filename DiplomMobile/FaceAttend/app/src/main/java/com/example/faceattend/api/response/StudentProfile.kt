package com.example.faceattend.api.response

data class StudentProfile(
    val userId: Int,
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val studentIdNumber: String?,
    val groupId: Int?
)