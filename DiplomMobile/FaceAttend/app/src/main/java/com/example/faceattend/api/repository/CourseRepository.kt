package com.example.faceattend.api.repository

import com.example.faceattend.api.ApiService
import com.example.faceattend.api.requests.CourseCreateRequest
import com.example.faceattend.api.requests.CourseFilter
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.GroupDto


class CourseRepository(
    private val apiService: ApiService
) {
    suspend fun getCoursesByGroup(groupId: Int): List<CourseDto> {
        val response = apiService.getCoursesByGroup(groupId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load courses: ${response.code()}")
    }

    suspend fun getCoursesByLector(lectorId: Int): List<CourseDto> {
        val response = apiService.getFilteredCourses(lectorId = lectorId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load courses: ${response.code()}")
    }

    suspend fun getAllCourses(): List<CourseDto> {
        val response = apiService.getFilteredCourses()
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load courses: ${response.code()}")
    }

    suspend fun getCourseById(courseId: Int): CourseDto {
        val response = apiService.getCourseById(courseId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Course not found")
        } else {
            throw Exception("Failed to load course: ${response.code()}")
        }
    }

    suspend fun getCourses(filter: CourseFilter): List<CourseDto> {
        val response = apiService.getFilteredCourses(filter.subjectId, filter.lectorId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load courses: ${response.code()}")
    }

    suspend fun createCourse(request: CourseCreateRequest) {
        val response = apiService.createCourse(
            request.subjectId, request.lectorId,
            request.startDate, request.endDate
        )
        if (!response.isSuccessful) throw Exception("Failed to create course: ${response.code()}")
    }

    suspend fun deleteCourse(id: Int) {
        val response = apiService.deleteCourse(id)
        if (!response.isSuccessful) throw Exception("Failed to create course: ${response.code()}")
    }

    suspend fun getCourseGroups(courseId: Int): List<GroupDto> {
        val response = apiService.getCourseGroups(courseId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception("Failed to load groups: ${response.code()}")
    }

    suspend fun removeGroup(courseId: Int, groupId: Int) {
        val response = apiService.removeGroupFromCourse(courseId, groupId)
        if (!response.isSuccessful) throw Exception("Failed to remove group: ${response.code()}")
    }

    suspend fun addGroup(courseId: Int, groupId: Int) {
        val response = apiService.addGroupToCourse(courseId, groupId)
        if (!response.isSuccessful) throw Exception("Failed to add group: ${response.code()}")
    }
}
