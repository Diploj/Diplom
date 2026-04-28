package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.response.UserProfile
import com.example.faceattend.data.TokenManager

class UserRepository (
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getUserProfile(): Result<UserProfile> {
        val userId = tokenManager.getUserId()
        val role = tokenManager.getRole()
        return try {
            when (role?.lowercase()) {
                "student" -> {
                    val response = apiService.getStudentProfile(userId)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Result.success(
                                UserProfile(
                                    userId = body.userId,
                                    name = body.name,
                                    surname = body.surname,
                                    patronymic = body.patronymic,
                                    email = body.email,
                                    role = role,
                                    studentIdNumber = body.studentIdNumber,
                                    groupId = body.groupId
                                )
                            )
                        } else Result.failure(Exception("Empty response"))
                    } else Result.failure(Exception("Failed to load profile: ${response.code()}"))
                }
                "lector" -> {
                    val response = apiService.getTeacherProfile(userId)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Result.success(
                                UserProfile(
                                    userId = body.userId,
                                    name = body.name,
                                    surname = body.surname,
                                    patronymic = body.patronymic,
                                    email = body.email,
                                    role = role,
                                    department = body.department
                                )
                            )
                        } else Result.failure(Exception("Empty response"))
                    } else Result.failure(Exception("Failed to load profile: ${response.code()}"))
                }
                "admin" -> {
                    val response = apiService.getAdminProfile(userId)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Result.success(
                                UserProfile(
                                    userId = body.userId,
                                    name = body.name,
                                    surname = body.surname,
                                    patronymic = body.patronymic,
                                    email = body.email,
                                    role = role
                                )
                            )
                        } else Result.failure(Exception("Empty response"))
                    } else Result.failure(Exception("Failed to load profile: ${response.code()}"))
                }
                else -> Result.failure(Exception("Unknown role"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}