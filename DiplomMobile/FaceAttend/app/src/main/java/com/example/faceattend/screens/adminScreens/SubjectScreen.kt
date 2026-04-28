package com.example.faceattend.screens.adminScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.faceattend.api.response.SubjectDto
import com.example.faceattend.views.admin.AdminSubjectsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminSubjectsViewModel = koinViewModel()
    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterName by viewModel.filterName.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var tempFilterName by remember { mutableStateOf("") }
    var newSubjectName by remember { mutableStateOf("") }
    var createError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Предметы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.List, contentDescription = "Фильтр")
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.Green)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadSubjects() }) {
                            Text("Повторить")
                        }
                    }
                }
                subjects.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Нет предметов")
                        if (filterName != null) {
                            Text("Фильтр: $filterName")
                            Button(onClick = { viewModel.setFilter(null) }) {
                                Text("Сбросить фильтр")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(subjects) { subject ->
                            SubjectItem(
                                subject = subject,
                                onDelete = {
                                    viewModel.deleteSubject(subject.id) { }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                createError = null
                newSubjectName = ""
            },
            title = { Text("Добавить предмет") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Название") },
                        singleLine = true,
                        isError = createError != null
                    )
                    if (createError != null) {
                        Text(
                            text = createError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            createError = null
                            viewModel.createSubject(
                                name = newSubjectName,
                                onSuccess = {
                                    showCreateDialog = false
                                    newSubjectName = ""
                                    createError = null
                                },
                                onError = { error ->
                                    createError = error
                                }
                            )
                        } else {
                            createError = "Название не может быть пустым"
                        }
                    }
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    createError = null
                    newSubjectName = ""
                }) { Text("Отмена") }
            }
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Фильтр по имени") },
            text = {
                OutlinedTextField(
                    value = tempFilterName,
                    onValueChange = { tempFilterName = it },
                    label = { Text("Название (частичное совпадение)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFilter(tempFilterName.takeIf { it.isNotBlank() })
                        showFilterDialog = false
                    }
                ) { Text("Применить") }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun SubjectItem(
    subject: SubjectDto,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* можно открыть детали, если нужно */ },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление предмета") },
            text = { Text("Вы уверены, что хотите удалить предмет «${subject.name}»?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}