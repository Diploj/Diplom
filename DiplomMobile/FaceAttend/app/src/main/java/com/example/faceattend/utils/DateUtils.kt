package com.example.faceattend.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatDate(dateString: String): String {
        return try {
            val localDate = LocalDate.parse(dateString.substring(0, 10))
            dateFormatter.format(localDate)
        } catch (e: Exception) {
            dateString
        }
    }
    fun formatTime(dateString: String): String {
        return try {
            val timePart = dateString.substring(11, 16)
            timePart
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateTime(dateString: String): String {
        return "${formatDate(dateString)} ${formatTime(dateString)}"
    }
}