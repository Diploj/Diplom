package com.example.faceattend.screens.adminScreens.selectors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.views.admin.selectors.GroupSelectorViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupSelectorDialog(
    lastSelectedGroup: GroupDto?,
    onSelect: (GroupDto) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: GroupSelectorViewModel = koinViewModel()
    val filterNumber by viewModel.filterNumber.collectAsState()
    val filterYear by viewModel.filterYear.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showResults by viewModel.showResults.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.reset()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите группу") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Блок последней выбранной группы
                if (lastSelectedGroup != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Последняя выбранная группа", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Группа №${lastSelectedGroup.number} (${lastSelectedGroup.year} год)",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = { onSelect(lastSelectedGroup) }) {
                                    Icon(Icons.Default.Done,contentDescription = "Выбрать", tint = Color.Green)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Поля фильтра
                OutlinedTextField(
                    value = filterNumber,
                    onValueChange = { viewModel.updateFilterNumber(it) },
                    label = { Text("Номер группы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = filterYear,
                    onValueChange = { viewModel.updateFilterYear(it) },
                    label = { Text("Год поступления") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.search() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Поиск")
                    }
                    if (showResults && (filterNumber.isNotBlank() || filterYear.isNotBlank())) {
                        OutlinedButton(
                            onClick = { viewModel.clearResults() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Очистить")
                        }
                    }
                }

                when {
                    !showResults -> {
                        Text("Введите фильтр и нажмите «Поиск»")
                    }
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                    }
                    groups.isEmpty() -> {
                        Text("Нет групп, соответствующих фильтру")
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(groups) { group ->
                                TextButton(
                                    onClick = { onSelect(group) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Группа №${group.number} (${group.year} год)",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        dismissButton = null
    )
}