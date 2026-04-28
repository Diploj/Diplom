package com.example.faceattend.di

import com.example.faceattend.api.RetrofitClient
import com.example.faceattend.api.repository.AttendanceRepository
import com.example.faceattend.api.repository.AuthRepository
import com.example.faceattend.api.repository.CourseRepository
import com.example.faceattend.api.repository.GroupRepository
import com.example.faceattend.api.repository.LectorRepository
import com.example.faceattend.api.repository.LectureRepository
import com.example.faceattend.api.repository.StudentRepository
import com.example.faceattend.api.repository.SubjectRepository
import com.example.faceattend.api.repository.UserRepository
import com.example.faceattend.configs.ApiConfig
import com.example.faceattend.data.TokenManager
import com.example.faceattend.data.UserProfileManager
import com.example.faceattend.views.AuthViewModel
import com.example.faceattend.views.CourseAttendanceViewModel
import com.example.faceattend.views.CourseDetailsViewModel
import com.example.faceattend.views.CoursesViewModel
import com.example.faceattend.views.HomeViewModel
import com.example.faceattend.views.LectureDetailsViewModel
import com.example.faceattend.views.admin.AdminCoursesViewModel
import com.example.faceattend.views.admin.selectors.GroupSelectorViewModel
import com.example.faceattend.views.admin.AdminGroupsViewModel
import com.example.faceattend.views.admin.AdminStudentsViewModel
import com.example.faceattend.views.admin.AdminSubjectsViewModel
import com.example.faceattend.views.admin.selectors.LectorSelectorViewModel
import com.example.faceattend.views.admin.selectors.SubjectSelectorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TokenManager(androidContext()) }
    single { UserProfileManager(androidContext()) }
    single {
        RetrofitClient.create(
            baseUrl = ApiConfig.BASE_URL,
            tokenManager = get()
        )
    }

    single { AuthRepository(apiService = get(), tokenManager = get()) }
    single { UserRepository(get(), get()) }
    single { StudentRepository(get(),get()) }
    single { CourseRepository(get()) }
    single { LectureRepository(get()) }
    single { AttendanceRepository(get()) }
    single { SubjectRepository(get()) }
    single { GroupRepository(get()) }
    single { LectorRepository(get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(),get(),get(),get()) }
    viewModel{ CoursesViewModel(get(),get()) }
    viewModel { CourseDetailsViewModel(get(), get()) }
    viewModel { LectureDetailsViewModel(get(), get(), get()) }
    viewModel { CourseAttendanceViewModel(get(), get())}
    viewModel{ AdminSubjectsViewModel(get()) }
    viewModel{ AdminGroupsViewModel(get()) }
    viewModel { AdminStudentsViewModel(get()) }
    viewModel { GroupSelectorViewModel(get()) }
    viewModel{ AdminCoursesViewModel(get()) }
    viewModel { SubjectSelectorViewModel(get()) }
    viewModel{ LectorSelectorViewModel(get()) }
}