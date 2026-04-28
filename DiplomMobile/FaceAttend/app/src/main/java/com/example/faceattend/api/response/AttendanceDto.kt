package com.example.faceattend.api.response

data class AttendanceDto(
    val id: Int?,
    val lectureId: Int,
    val studentId: Int,
    val attended: Boolean
)