package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.requests.LectorRegisterRequest
import com.example.faceattend.api.requests.LoginRequest
import com.example.faceattend.api.requests.StudentRegisterRequest
import com.example.faceattend.api.response.LoginResponse
import com.example.faceattend.api.response.RegisterResponse
import com.example.faceattend.data.TokenManager

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.token)
                    tokenManager.saveRole(body.role)
                    tokenManager.saveUserId(body.id)
                    tokenManager.saveExpiration(body.expiration)
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerStudent(
        name: String,
        surname: String,
        patronymic: String,
        email: String,
        password: String,
        studentIdNumber: String?
    ): Result<RegisterResponse> {
        return try {
            val request = StudentRegisterRequest(
                name = name,
                surname = surname,
                patronymic = patronymic,
                email = email,
                password = password,
                studentIdNumber = studentIdNumber
            )
            val response = apiService.registerStudent(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerTeacher(
        name: String,
        surname: String,
        patronymic: String,
        email: String,
        password: String,
        department: String?
    ): Result<RegisterResponse> {
        return try {
            val request = LectorRegisterRequest(
                name = name,
                surname = surname,
                patronymic = patronymic,
                email = email,
                password = password,
                department = department
            )
            val response = apiService.registerTeacher(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}