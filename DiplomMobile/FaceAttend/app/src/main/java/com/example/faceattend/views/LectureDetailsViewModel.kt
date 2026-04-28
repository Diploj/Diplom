package com.example.faceattend.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.AttendanceRepository
import com.example.faceattend.api.repository.LectureRepository
import com.example.faceattend.api.repository.StudentRepository
import com.example.faceattend.api.response.AttendanceDto
import com.example.faceattend.api.response.LectureDto
import com.example.faceattend.api.response.StudentAttendanceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LectureDetailsViewModel(
    private val lectureRepository: LectureRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _lecture = MutableStateFlow<LectureDto?>(null)
    val lecture: StateFlow<LectureDto?> = _lecture.asStateFlow()

    private val _photoBytes = MutableStateFlow<ByteArray?>(null)
    val photoBytes: StateFlow<ByteArray?> = _photoBytes.asStateFlow()

    private val _students = MutableStateFlow<List<StudentAttendanceDto>>(emptyList())
    val students: StateFlow<List<StudentAttendanceDto>> = _students.asStateFlow()

    private val _pendingAttendance = MutableStateFlow<List<StudentAttendanceDto>?>(null)
    val pendingAttendance: StateFlow<List<StudentAttendanceDto>?> = _pendingAttendance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun loadLecture(lectureId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lecture = lectureRepository.getLectureById(lectureId)
                _lecture.value = lecture
                if (lecture.isPhotoLoaded) {
                    loadPhoto(lectureId)
                    if (lecture.isAttended) {
                        loadAttendance(lecture.id, lecture.courseId)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadPhoto(lectureId: Int) {
        viewModelScope.launch {
            try {
                val bytes = lectureRepository.getLecturePhoto(lectureId)
                _photoBytes.value = bytes
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить фото: ${e.message}"
            }
        }
    }

    private fun loadAttendance(lectureId: Int, courseId: Int) {
        viewModelScope.launch {
            try {
                val attendance = attendanceRepository.getAttendanceLecture(lectureId)
                val studentsFromCourse = studentRepository.getStudentsByCourse(courseId)
                val attendanceMap = attendance.associate { it.studentId to it }
                val studentList = studentsFromCourse.map { student ->
                    StudentAttendanceDto(
                        id = attendanceMap[student.userId]?.id,
                        studentId = student.userId,
                        fullName = "${student.surname} ${student.name} ${student.patronymic}",
                        attended = attendanceMap[student.userId]?.attended ?: false
                    )
                }
                _students.value = studentList
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addLecturePhoto(lectureId: Int, photoFile: File, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            try {
                lectureRepository.addLecturePhoto(lectureId, photoFile)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun autoAttendance(lectureId: Int, onComplete: (List<StudentAttendanceDto>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val attendanceDtoList = attendanceRepository.autoAttendance(lectureId)
                val lecture = _lecture.value ?: throw Exception("Лекция не загружена")
                val studentsFromCourse = studentRepository.getStudentsByCourse(lecture.courseId)
                val studentMap = studentsFromCourse.associateBy { it.userId }
                val studentAttendanceList = attendanceDtoList.map { dto ->
                    val student = studentMap[dto.studentId] ?: throw Exception("Студент не найден")
                    StudentAttendanceDto(
                        id = dto.id,
                        studentId = dto.studentId,
                        fullName = "${student.surname} ${student.name} ${student.patronymic}",
                        attended = dto.attended
                    )
                }
                _pendingAttendance.value = studentAttendanceList
                onComplete(studentAttendanceList)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAttendance(attendanceList: List<AttendanceDto>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                attendanceRepository.updateAttendance(attendanceList)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAttendance(attendanceList: List<AttendanceDto>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                attendanceRepository.addAttendance(attendanceList)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPendingAttendance() {
        _pendingAttendance.value = null
    }
}