package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.response.AttendanceDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AttendanceRepository(
    private val apiService: ApiService
) {
    suspend fun getAttendanceLecture(lectureId: Int): List<AttendanceDto> {
        val response = apiService.getAttendanceLecture(lectureId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load attendance: ${response.code()}")
    }

    suspend fun addAttendance(attendance: List<AttendanceDto>) {
        val response = apiService.addAttendance(attendance)
        if (!response.isSuccessful) throw Exception("Failed to add attendance: ${response.code()}")
    }

    suspend fun updateAttendance(attendance: List<AttendanceDto>) {
        val response = apiService.updateAttendance(attendance)
        if (!response.isSuccessful) throw Exception("Failed to update attendance: ${response.code()}")
    }

    suspend fun autoAttendance(lectureId: Int): List<AttendanceDto> {
        val response = apiService.autoAttendance(lectureId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Auto attendance failed: ${response.code()}")
    }

    suspend fun getAttendanceByCourse(courseId: Int): Map<String, List<AttendanceDto>> {
        val response = apiService.getAttendanceByCourse(courseId)
        if (response.isSuccessful) return response.body() ?: emptyMap()
        else throw Exception("Failed to load attendance: ${response.code()}")
    }

    suspend fun getGroupAttendanceByCourse(groupId: Int, courseId: Int): Map<String, List<AttendanceDto>> {
        val response = apiService.getGroupAttendanceByCourse(groupId, courseId)
        if (response.isSuccessful) return response.body() ?: emptyMap()
        else throw Exception("Failed to load group attendance: ${response.code()}")
    }
}