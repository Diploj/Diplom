package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.response.LectureDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LectureRepository(
    private val apiService: ApiService
) {
    suspend fun getLecturesByCourse(courseId: Int, isActual: Boolean): List<LectureDto> {
        val response = apiService.getFilteredLectures(courseId, isActual = isActual)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load lectures: ${response.code()}")
        }
    }

    suspend fun createLecture(courseId: Int, date: String) {
        val response = apiService.createLecture(courseId, date)
        if (!response.isSuccessful) {
            throw Exception("Failed to create lecture: ${response.code()}")
        }
    }

    suspend fun deleteLecture(lectureId: Int) {
        val response = apiService.deleteLecture(lectureId)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete lecture: ${response.code()}")
        }
    }

    suspend fun addLecturePhoto(lectureId: Int, photoFile: File) {
        val requestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)
        val response = apiService.addLecturePhoto(lectureId, part)
        if (!response.isSuccessful) throw Exception("Failed to upload photo: ${response.code()}")
    }

    suspend fun getLectureById(lectureId: Int): LectureDto {
        val response = apiService.getLectureById(lectureId)
        if (response.isSuccessful) return response.body() ?: throw Exception("Lecture not found")
        else throw Exception("Failed to load lecture: ${response.code()}")
    }

    suspend fun getLecturesByCourseAttended(courseId: Int): List<LectureDto> {
        val response = apiService.getFilteredLectures(courseId, true)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load lectures: ${response.code()}")
    }

    suspend fun getLecturePhoto(lectureId: Int): ByteArray {
        val response = apiService.getLecturePhoto(lectureId)
        if (response.isSuccessful) return response.body()?.bytes() ?: throw Exception("No photo")
        else throw Exception("Failed to load photo: ${response.code()}")
    }
}