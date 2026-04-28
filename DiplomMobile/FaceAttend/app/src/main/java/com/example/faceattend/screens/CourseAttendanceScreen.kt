package com.example.faceattend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.faceattend.api.response.LectureDto
import com.example.faceattend.data.UserProfileManager
import com.example.faceattend.utils.DateUtils.formatDate
import com.example.faceattend.utils.DateUtils.formatDateTime
import com.example.faceattend.views.CourseAttendanceViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAttendanceScreen(
    courseId: Int,
    onBack: () -> Unit
) {
    val viewModel: CourseAttendanceViewModel = koinViewModel()
    val lectures by viewModel.lectures.collectAsState()
    val rows by viewModel.rows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val userProfileManager = koinInject<UserProfileManager>()
    val profile = remember { userProfileManager.getProfile() }
    val role = profile?.role?.lowercase() ?: ""
    val groupId = profile?.groupId

    var selectedLecture by remember { mutableStateOf<LectureDto?>(null) }
    val horizontalScrollState = rememberScrollState()

    val rowHeight = 42.dp
    val leftColumnWidth = 140.dp
    val rightColumnWidth = 75.dp

    LaunchedEffect(courseId) {
        viewModel.loadData(courseId, groupId, role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Посещаемость курса") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                        Button(onClick = { viewModel.loadData(courseId, groupId, role) }) {
                            Text("Повторить")
                        }
                    }
                }
                lectures.isEmpty() -> Text("Нет лекций с посещаемостью", modifier = Modifier.align(Alignment.Center))
                rows.isEmpty() -> Text("Нет данных о студентах", modifier = Modifier.align(Alignment.Center))
                else -> {
                    Column {
                        // Заголовок
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .height(rowHeight)
                        ) {
                            // Левая верхняя ячейка
                            Box(
                                modifier = Modifier
                                    .width(leftColumnWidth)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.Gray)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Студент", style = MaterialTheme.typography.titleSmall)
                                    Text("Пропуски", style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp), color = Color.Gray)
                                }
                            }

                            // Горизонтальный скролл для дат
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                lectures.forEach { lecture ->
                                    Box(
                                        modifier = Modifier
                                            .width(rightColumnWidth)
                                            .fillMaxHeight()
                                            .border(0.5.dp, Color.Gray)
                                            .clickable { selectedLecture = lecture }
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = formatDate(lecture.date),
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.Gray)
                            )
                        }

                        // Строки студентов
                        LazyColumn {
                            items(rows) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(rowHeight)
                                        .background(
                                            if (row.studentId % 2 == 0) Color.Transparent
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                ) {
                                    // Левая ячейка (ФИО + пропуски)
                                    Box(
                                        modifier = Modifier
                                            .width(leftColumnWidth)
                                            .fillMaxHeight()
                                            .border(0.5.dp, Color.Gray)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = row.fullName,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "${row.missedCount}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = if (row.missedCount > 0) Color.Red else Color.Green
                                            )
                                        }
                                    }

                                    // Горизонтальный скролл для отметок (синхронно)
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(horizontalScrollState)
                                    ) {
                                        lectures.forEach { lecture ->
                                            val attended = row.attendanceMap[lecture.id] ?: false
                                            Box(
                                                modifier = Modifier
                                                    .width(rightColumnWidth)
                                                    .fillMaxHeight()
                                                    .border(0.5.dp, Color.Gray)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (attended) "✅" else "❌",
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }

                                    // Пустая ячейка, заполняющая оставшееся пространство (для границы)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(0.5.dp, Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedLecture != null) {
        AlertDialog(
            onDismissRequest = { selectedLecture = null },
            title = { Text("Дата и время лекции") },
            text = { Text(formatDateTime(selectedLecture!!.date)) },
            confirmButton = {
                TextButton(onClick = { selectedLecture = null }) {
                    Text("OK")
                }
            }
        )
    }
}