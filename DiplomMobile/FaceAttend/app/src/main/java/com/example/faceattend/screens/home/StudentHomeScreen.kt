package com.example.faceattend.screens.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.faceattend.api.response.UserProfile
import com.example.faceattend.views.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun StudentHomeScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onStudentCourses: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isPhotosLoading by viewModel.isPhotosLoading.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingPhotoId by remember { mutableStateOf<Int?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.uploadPhoto(file)
            } else {
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadPhotos()
    }


    val drawerItems = listOf(
        DrawerItem("Мои курсы", onStudentCourses)
    )

    HomeScaffold(
        drawerItems = drawerItems,
        onEditProfile = onEditProfile,
        onLogout = onLogout
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text("Загрузка", modifier = Modifier.padding(top = 8.dp))
                }
                error != null -> {
                    ErrorContent(error = error!!, onRetry = { viewModel.loadProfile() })
                }
                userProfile != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PhotosSection(
                            photos = photos,
                            isLoading = isPhotosLoading,
                            isDeleting = isDeleting,
                            isUploading = isUploading,
                            onDeletePhoto = { photoId ->
                                pendingPhotoId = photoId
                                showDeleteDialog = true
                            },
                            onAddPhoto = { imagePickerLauncher.launch("image/*") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StudentProfileCard(profile = userProfile!!)
                    }
                }
                else -> {
                    Text("Нет данных")
                }
            }

        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { var showDeleteDialog = false },
            title = { Text("Удалить фото") },
            text = { Text("Вы уверены?") },
            confirmButton = {
                Button(onClick = {
                    pendingPhotoId?.let { viewModel.deletePhoto(it) }
                    showDeleteDialog = false
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
@Composable
private fun StudentProfileCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Студент",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Добро пожаловать,",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${profile.name} ${profile.patronymic}!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: ${profile.email}")
            if (profile.groupId != null) {
                Text(text = "Группа: ${profile.groupId}")
            }
        }
    }
}