package com.example.faceattend.screens.home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.faceattend.api.response.FaceImageDto
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.collections.List

@Composable
fun PhotosSection(
    photos: List<FaceImageDto>,
    isLoading: Boolean,
    isDeleting: Boolean,
    isUploading: Boolean,
    onDeletePhoto: (Int) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxPhotos = 5
    val totalItems = photos.size + if (photos.size < maxPhotos) 1 else 0

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Мои фотографии (максимум $maxPhotos)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (totalItems == 1) {
                // Центрируем единственную карточку
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (photos.size < maxPhotos) {
                        if (isUploading) {
                            UploadingCard()
                        } else {
                            AddPhotoCard(onAddPhoto = onAddPhoto)
                        }
                    } else {
                        // fallback – если вдруг есть одна фотография
                        PhotoCard(
                            photo = photos.first(),
                            isDeleting = isDeleting,
                            onDeleteClick = { onDeletePhoto(photos.first().id) }
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = photos,
                        key = { it.id }
                    ) { photo ->
                        PhotoCard(
                            photo = photo,
                            isDeleting = isDeleting,
                            onDeleteClick = { onDeletePhoto(photo.id) }
                        )
                    }
                    if (photos.size < maxPhotos) {
                        item {
                            if (isUploading) {
                                UploadingCard()
                            } else {
                                AddPhotoCard(onAddPhoto = onAddPhoto)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPhotoCard(onAddPhoto: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable(onClick = onAddPhoto),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить фото",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Добавить фото",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UploadingCard() {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text("Загрузка...", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PhotoCard(
    photo: FaceImageDto,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val fullUrl = photo.getFullUrl()
    var retryTrigger by remember { mutableIntStateOf(0) }

    val urlWithRetry = remember(fullUrl, retryTrigger) {
        "$fullUrl${if (retryTrigger > 0) "&retry=$retryTrigger" else ""}"
    }

    val imageRequest = remember(urlWithRetry) {
        ImageRequest.Builder(context)
            .data(urlWithRetry)
            .crossfade(true)
            .build()
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = "Student photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                },
                error = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Ошибка загрузки",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Не загрузилось", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { retryTrigger++ },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            )

            IconButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = Color.Red
                )
            }

            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}