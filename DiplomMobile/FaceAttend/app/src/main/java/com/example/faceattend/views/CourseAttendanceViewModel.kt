package com.example.faceattend.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.AttendanceRepository
import com.example.faceattend.api.repository.LectureRepository
import com.example.faceattend.api.response.LectureDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentAttendanceRow(
    val studentId: Int,
    val fullName: String,
    val attendanceMap: Map<Int, Boolean>, // lectureId -> attended
    val missedCount: Int
)

class CourseAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val lectureRepository: LectureRepository
) : ViewModel() {

    private val _lectures = MutableStateFlow<List<LectureDto>>(emptyList())
    val lectures: StateFlow<List<LectureDto>> = _lectures.asStateFlow()

    private val _rows = MutableStateFlow<List<StudentAttendanceRow>>(emptyList())
    val rows: StateFlow<List<StudentAttendanceRow>> = _rows.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadData(courseId: Int, groupId: Int? = null, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {

                val lectures = lectureRepository.getLecturesByCourseAttended(courseId)
                _lectures.value = lectures
                val totalLectures = lectures.count()


                val attendanceMap = if (role == "student" && groupId != null) {
                    attendanceRepository.getGroupAttendanceByCourse(groupId, courseId)
                } else {
                    attendanceRepository.getAttendanceByCourse(courseId)
                }

                val rowsList = attendanceMap.map { (studentName, attendanceList) ->
                    val attendanceByLecture = attendanceList.associate { it.lectureId to it.attended }
                    val studentId = attendanceList.firstOrNull()?.studentId ?: 0
                    val attendedCount = attendanceList.count { it.attended }
                    val missedCount = totalLectures - attendedCount
                    StudentAttendanceRow(
                        studentId = studentId,
                        fullName = studentName,
                        attendanceMap = attendanceByLecture,
                        missedCount = missedCount
                    )
                }
                _rows.value = rowsList
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}