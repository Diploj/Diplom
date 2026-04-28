package com.example.faceattend.api.response

data class CourseDto(
    val id: Int,
    val subjectId: Int,
    val subjectName: String,
    val lectorId: Int,
    val lectorFullName: String,
    val startDate: String,
    val endDate: String
)