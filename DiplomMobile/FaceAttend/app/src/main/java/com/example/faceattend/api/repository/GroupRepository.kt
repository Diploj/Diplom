package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.response.GroupDto

class GroupRepository(
    private val apiService: ApiService
) {
    suspend fun getGroups(number: Int? = null, year: Int? = null): List<GroupDto> {
        val response = apiService.getFilteredGroups(number, year)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load groups: ${response.code()}")
    }

    suspend fun createGroup(number: Int, year: Int, createDate: String) {
        val response = apiService.createGroup(number, year, createDate)
        if (!response.isSuccessful) throw Exception("Failed to create group: ${response.code()}")
    }

    suspend fun deleteGroup(id: Int) {
        val response = apiService.deleteGroup(id)
        if (!response.isSuccessful) throw Exception("Failed to delete group: ${response.code()}")
    }
}