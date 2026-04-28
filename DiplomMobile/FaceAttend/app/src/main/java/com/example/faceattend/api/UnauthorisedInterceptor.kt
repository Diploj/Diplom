package com.example.faceattend.api
import com.example.faceattend.data.TokenManager
import com.example.faceattend.exceptoins.UnauthorizedException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class UnauthorisedInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            tokenManager.clear()
            throw UnauthorizedException()
        }
        return response
    }
}