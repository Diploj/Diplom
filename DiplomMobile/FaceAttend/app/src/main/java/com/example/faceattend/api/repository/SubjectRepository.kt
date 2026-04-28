package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.response.SubjectDto

class SubjectRepository(
    private val apiService: ApiService
) {
    suspend fun getSubjects(name: String? = null): List<SubjectDto> {
        val response = apiService.getFilteredSubjects(name)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load subjects: ${response.code()}")
    }

    suspend fun createSubject(name: String) {
        val response = apiService.createSubject(name)
        if (!response.isSuccessful) throw Exception("Failed to create subject: ${response.code()}")
    }

    suspend fun deleteSubject(id: Int) {
        val response = apiService.deleteSubject(id)
        if (!response.isSuccessful) throw Exception("Failed to delete subject: ${response.code()}")
    }
}