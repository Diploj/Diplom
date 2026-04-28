package com.example.faceattend.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.faceattend.navigation.Routes
import com.example.faceattend.utils.SessionExpiredManager

@Composable
fun AlertScreen(
    onTokenExpiration : () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* запрещаем закрытие без нажатия кнопки */ },
        title = { Text("Сессия истекла") },
        text = { Text("Ваша сессия истекла. Пожалуйста, войдите снова.") },
        confirmButton = {
            TextButton(
                onClick = onTokenExpiration
            ) {
                Text("OK")
            }
        }
    )
}