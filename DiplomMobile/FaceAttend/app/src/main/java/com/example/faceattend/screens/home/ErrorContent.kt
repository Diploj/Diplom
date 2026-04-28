package com.example.faceattend.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ошибка загрузки: $error",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}