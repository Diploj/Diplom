package com.example.faceattend.api.requests

data class StudentRegisterRequest(
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val password: String,
    val studentIdNumber: String?
)

data class LectorRegisterRequest(
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val password: String,
    val department: String?
)