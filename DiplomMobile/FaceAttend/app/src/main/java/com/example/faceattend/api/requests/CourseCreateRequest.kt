package com.example.faceattend.api.requests

data class CourseCreateRequest(
    val subjectId: Int,
    val lectorId: Int,
    val startDate: String,
    val endDate: String
)