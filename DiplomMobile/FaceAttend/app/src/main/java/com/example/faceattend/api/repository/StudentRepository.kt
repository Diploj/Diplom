package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.requests.StudentFilter
import com.example.faceattend.api.response.FaceImageDto
import com.example.faceattend.api.response.StudentProfile
import com.example.faceattend.data.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class StudentRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getStudentPhotoUrls(): Result<List<FaceImageDto>> {
        val userId = tokenManager.getUserId()
        return try {
            val response = apiService.getStudentPhotoUrls(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from getStudentPhotoUrls"))
                }
            } else {
                Result.failure(Exception("Failed to load photos: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentsByCourse(courseId: Int): List<StudentProfile> {
        val response = apiService.getStudentsByCourse(courseId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load students: ${response.code()}")
    }

    suspend fun getPhotoBytes(imageUrl: String): Result<ByteArray> {
        return try {
            val response = apiService.getPhoto(imageUrl)
            if (response.isSuccessful) {
                val bytes = response.body()?.bytes()
                if (bytes != null) {
                    Result.success(bytes)
                } else {
                    Result.failure(Exception("Empty image body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch photo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(photoFile: File): Result<Unit> {
        return try {
            val requestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
            val response = apiService.addPhoto(part)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePhoto(photoId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteStudentPhoto(photoId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudents(filter: StudentFilter): List<StudentProfile> {
        val response = apiService.getFilteredStudents(
            name = filter.name,
            surname = filter.surname,
            patronymic = filter.patronymic,
            email = filter.email,
            groupId = filter.groupId,
            studentIdNumber = filter.studentIdNumber
        )
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load students: ${response.code()}")
    }

    suspend fun setStudentGroup(studentId: Int, groupId: Int) {
        val response = apiService.setStudentGroup(studentId, groupId)
        if (!response.isSuccessful) throw Exception("Failed to set group: ${response.code()}")
    }
}