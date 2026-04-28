package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.requests.LectorFilter
import com.example.faceattend.api.response.LectorProfile

class LectorRepository(
    private val apiService: ApiService
) {
    suspend fun searchLectors(filter: LectorFilter): List<LectorProfile> {
        val response = apiService.getFilteredLectors(
            name = filter.name,
            surname = filter.surname,
            patronymic = filter.patronymic,
            email = filter.email,
            department = filter.department
        )
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to search lectors: ${response.code()}")
    }
}