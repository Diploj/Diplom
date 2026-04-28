package com.example.faceattend.api.requests

data class StudentFilter(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
    val groupId: Int? = null,
    val studentIdNumber: String? = null
)