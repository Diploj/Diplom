package com.example.faceattend.api.response

data class LectureDto(
    val id: Int,
    val courseId: Int,
    val isPhotoLoaded: Boolean,
    val isAttended: Boolean,
    val date: String
)