package com.example.faceattend.navigation


sealed class Routes(var route : String) {
    data object Auth : Routes(AUTH)
    data object Home : Routes(HOME)
    data object Courses : Routes(COURSES)
    data object  CourseWithId : Routes(COURSE_WITH_ID)
    data object  Course : Routes(COURSE)
    data object Lecture : Routes(LECTURE)
    data object Attendance : Routes(ATTENDANCE)
    data object  Subject : Routes(SUBJECT)
    data object  Group : Routes(GROUP)
    data object  Students : Routes(STUDENTS)
    data object Camera : Routes(CAMERA)
    private companion object {
        const val AUTH = "auth"
        const val HOME = "home"
        const val COURSES = "courses"
        const val COURSE_WITH_ID = "course/{courseId}"
        const val COURSE = "course"
        const val LECTURE = "lecture/{lectureId}"
        const val ATTENDANCE = "attendance/{courseId}"
        const val SUBJECT = "subject"
        const val GROUP = "group"
        const val STUDENTS = "students"
        const val CAMERA = "camera/{lectureId}"
    }
}