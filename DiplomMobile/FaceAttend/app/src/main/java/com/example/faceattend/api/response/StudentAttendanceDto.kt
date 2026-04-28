package com.example.faceattend.api.response

data class StudentAttendanceDto(
    val id: Int?,
    val studentId: Int,
    val fullName: String,
    var attended: Boolean
)