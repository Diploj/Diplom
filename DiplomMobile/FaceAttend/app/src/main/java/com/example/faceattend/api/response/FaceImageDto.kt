package com.example.faceattend.api.response

import com.example.faceattend.configs.ApiConfig

data class FaceImageDto (
    val id : Int,
    val imageUrl : String
    )
{
    fun getFullUrl() : String
    {
        return "${ApiConfig.BASE_URL}Student/getPhoto?imageUrl=${imageUrl}"
    }
}