package com.example.faceattend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.example.faceattend.api.response.LectureDto
import com.example.faceattend.data.UserProfileManager
import com.example.faceattend.utils.DateUtils.formatDate
import com.example.faceattend.views.CourseDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    courseId: Int,
    onBack: () -> Unit,
    onLectureClick: (Int) -> Unit,
    onAttendanceClick: () -> Unit
) {
    val viewModel: CourseDetailsViewModel = koinViewModel()
    val course by viewModel.course.collectAsState()
    val lectures by viewModel.lectures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isActual by viewModel.isActual.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val userProfileManager = koinInject<UserProfileManager>()
    val role = remember { userProfileManager.getProfile()?.role?.lowercase() }

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.subjectName ?: "Загрузка...")},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (role == "lector") {
                        IconButton(
                            onClick = { showDatePicker = true; },
                            enabled = !isCreating
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить лекцию",
                                tint = Color.Green)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadCourse(courseId) }) {
                            Text("Повторить")
                        }
                    }
                }
                course != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Карточка курса
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Преподаватель: ${course!!.lectorFullName}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Продолжительность: ${formatDate(course!!.startDate)} - ${formatDate(course!!.endDate)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))


                        Button(
                            onClick = onAttendanceClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Посещаемость курса")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Только будущие лекции", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = isActual,
                                onCheckedChange = { viewModel.setFilter(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Список лекций
                        if (lectures.isEmpty()) {
                            Text("Нет лекций", modifier = Modifier.fillMaxWidth())
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(lectures) { lecture ->
                                    LectureCard(
                                        lecture = lecture,
                                        isLector = role == "lector",
                                        onLectureClick = { onLectureClick(lecture.id) },
                                        onDeleteClick = { viewModel.deleteLecture(lecture.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                else -> Text("Курс не найден")
            }
        }
    }

    // Диалог выбора даты и времени
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            showDatePicker = false
                            showTimePicker = true
                        } else {
                            showDatePicker = false
                        }
                    }
                ) { Text("Далее") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = 10, initialMinute = 0)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выберите время") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = selectedDateMillis!!
                        val utcDateTime = Instant.ofEpochMilli(selectedDate)
                            .atOffset(ZoneOffset.UTC)
                            .withHour(timePickerState.hour)
                            .withMinute(timePickerState.minute)
                            .withSecond(0)
                            .toString()
                        viewModel.createLecture(utcDateTime)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun LectureCard(
    lecture: LectureDto,
    isLector: Boolean,
    onLectureClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val formattedDate = remember(lecture.date) {
        try {
            val instant = Instant.parse(lecture.date)
            val dateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dateTime)
        } catch (e: Exception) {
            lecture.date.take(10)
        }
    }
    val formattedTime = remember(lecture.date) {
        try {
            val instant = Instant.parse(lecture.date)
            val dateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("HH:mm").format(dateTime)
        } catch (e: Exception) {
            lecture.date.drop(11).take(5)
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLectureClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formattedDate, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(24.dp)) // увеличенный отступ
                Text("⏰", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formattedTime, style = MaterialTheme.typography.titleLarge)
            }
            Box(modifier = Modifier.size(24.dp)) {
                if (isLector) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить лекцию",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление лекции") },
            text = { Text("Вы уверены, что хотите удалить эту лекцию?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteClick() }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}
