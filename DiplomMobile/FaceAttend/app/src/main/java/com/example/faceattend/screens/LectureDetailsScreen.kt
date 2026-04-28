package com.example.faceattend.screens

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.faceattend.api.response.AttendanceDto
import com.example.faceattend.api.response.StudentAttendanceDto
import com.example.faceattend.data.UserProfileManager
import com.example.faceattend.utils.DateUtils
import com.example.faceattend.utils.DateUtils.formatDate
import com.example.faceattend.utils.DateUtils.formatTime
import com.example.faceattend.views.LectureDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.collections.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailsScreen(
    lectureId: Int,
    onBack: () -> Unit
) {
    val viewModel: LectureDetailsViewModel = koinViewModel()
    val lecture by viewModel.lecture.collectAsState()
    val photoBytes by viewModel.photoBytes.collectAsState()
    val students by viewModel.students.collectAsState()
    val pendingAttendance by viewModel.pendingAttendance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    val context = LocalContext.current
    val userProfileManager = koinInject<UserProfileManager>()
    val role = remember { userProfileManager.getProfile()?.role?.lowercase() }

    var showAttendanceEditor by remember { mutableStateOf(false) }
    var showAttendanceView by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val tempCameraFile = remember { File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg") }
    val cameraUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempCameraFile
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = File(context.cacheDir, "lecture_photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.addLecturePhoto(lectureId, file) {
                viewModel.loadLecture(lectureId)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.addLecturePhoto(lectureId, tempCameraFile) {
                viewModel.loadLecture(lectureId)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(cameraUri)
        }
    }

    fun takePhoto() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(cameraUri)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    fun openPhotoSourceDialog() {
        showPhotoSourceDialog = true
    }

    LaunchedEffect(lectureId) {
        viewModel.loadLecture(lectureId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали лекции") },
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadLecture(lectureId) }) {
                            Text("Повторить")
                        }
                    }
                }
                lecture != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "📅 ${formatDate(lecture!!.date)}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "⏰ ${formatTime(lecture!!.date)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!lecture!!.isPhotoLoaded) {
                            if (role == "lector") {
                                if (isUploading) {
                                    CircularProgressIndicator()
                                    Text("Загрузка фото...")
                                } else {
                                    Button(
                                        onClick = { openPhotoSourceDialog() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Загрузить фото")
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "Фото лекции еще не загружено преподавателем.",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            photoBytes?.let { bytes ->
                                Image(
                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap(),
                                    contentDescription = "Фото лекции",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (role == "lector") {
                                Button(
                                    onClick = { openPhotoSourceDialog() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Заменить фото")
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (!lecture!!.isAttended) {
                                    Button(
                                        onClick = {
                                            viewModel.autoAttendance(lectureId) {
                                                showAttendanceEditor = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Распознать посещаемость")
                                    }
                                } else {
                                    Button(
                                        onClick = { showAttendanceEditor = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Редактировать посещаемость")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.autoAttendance(lectureId) {
                                                showAttendanceEditor = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Пересоздать посещаемость")
                                    }
                                }
                            } else {
                                if (lecture!!.isAttended) {
                                    Button(
                                        onClick = { showAttendanceView = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Посмотреть посещаемость")
                                    }
                                } else {
                                    Text("Преподаватель еще не отметил посещаемость.")
                                }
                            }
                        }
                    }
                }
                else -> Text("Лекция не найдена")
            }
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Выберите источник") },
            text = { Text("Загрузить фото из галереи или сделать снимок камерой") },
            confirmButton = {
                TextButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showPhotoSourceDialog = false
                    }
                ) { Text("Галерея") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        takePhoto()
                        showPhotoSourceDialog = false
                    }
                ) { Text("Камера") }
            }
        )
    }

    val editorData = pendingAttendance ?: if (lecture?.isAttended == true) students else null
    if (showAttendanceEditor && editorData != null) {
        AttendanceEditorScreen(
            students = editorData,
            lectureId = lectureId,
            onConfirm = { updatedList ->
                val attendanceDtoList = updatedList.map { student ->
                    AttendanceDto(
                        id = student.id,
                        lectureId = lectureId,
                        studentId = student.studentId,
                        attended = student.attended
                    )
                }
                val hasIds = updatedList.any { it.id != null }
                if (hasIds) {
                    viewModel.updateAttendance(attendanceDtoList) {
                        showAttendanceEditor = false
                        viewModel.clearPendingAttendance()
                        viewModel.loadLecture(lectureId)
                    }
                } else {
                    viewModel.addAttendance(attendanceDtoList) {
                        showAttendanceEditor = false
                        viewModel.clearPendingAttendance()
                        viewModel.loadLecture(lectureId)
                    }
                }
            },
            onCancel = {
                showAttendanceEditor = false
                viewModel.clearPendingAttendance()
            }
        )
    }

    if (showAttendanceView && students.isNotEmpty()) {
        AttendanceViewScreen(
            students = students,
            onClose = { showAttendanceView = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceEditorScreen(
    students: List<StudentAttendanceDto>,
    lectureId: Int,
    onConfirm: (List<StudentAttendanceDto>) -> Unit,
    onCancel: () -> Unit
) {
    var localStudents by remember { mutableStateOf(students) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование посещаемости") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = { onConfirm(localStudents) }) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(localStudents) { student ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.fullName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Checkbox(
                        checked = student.attended,
                        onCheckedChange = { isChecked ->
                            localStudents = localStudents.map {
                                if (it.studentId == student.studentId) it.copy(attended = isChecked)
                                else it
                            }
                        }
                    )
                }
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceViewScreen(
    students: List<StudentAttendanceDto>,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Посещаемость") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.fullName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (student.attended) "✅ Присутствовал" else "❌ Отсутствовал",
                        color = if (student.attended) Color.Green else Color.Red
                    )
                }
                Divider()
            }
        }
    }
}

