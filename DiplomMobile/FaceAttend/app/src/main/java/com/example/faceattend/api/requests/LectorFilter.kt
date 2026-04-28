package com.example.faceattend.api.requests

data class LectorFilter(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
    val department: String? = null
)