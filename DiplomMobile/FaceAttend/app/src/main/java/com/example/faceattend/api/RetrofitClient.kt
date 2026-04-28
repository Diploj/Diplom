package com.example.faceattend.api

import com.example.faceattend.data.TokenManager
import com.example.faceattend.utils.SessionExpiredManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
object RetrofitClient {
    fun create(baseUrl: String, tokenManager: TokenManager): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = tokenManager.getToken()
                val request = if (!token.isNullOrEmpty()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                val response = chain.proceed(request)

                // Обработка 401 Unauthorized
                if (response.code == 401) {
                    tokenManager.clear()
                    // Уведомляем приложение об истечении токена
                    CoroutineScope(Dispatchers.IO).launch {
                        SessionExpiredManager.trigger()
                    }
                    // Выбрасываем исключение, чтобы прекратить выполнение запроса
                    throw IOException("Session expired. Please log in again.")
                }
                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}