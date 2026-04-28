package com.example.faceattend.api

import com.example.faceattend.api.requests.LectorRegisterRequest
import com.example.faceattend.api.requests.LoginRequest
import com.example.faceattend.api.requests.StudentRegisterRequest
import com.example.faceattend.api.response.AdminProfile
import com.example.faceattend.api.response.AttendanceDto
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.FaceImageDto
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.api.response.LectorProfile
import com.example.faceattend.api.response.LectureDto
import com.example.faceattend.api.response.LoginResponse
import com.example.faceattend.api.response.RegisterResponse
import com.example.faceattend.api.response.StudentProfile
import com.example.faceattend.api.response.SubjectDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("Auth/register/student")
    suspend fun registerStudent(@Body request: StudentRegisterRequest): Response<RegisterResponse>
    @POST("Auth/register/lector")
    suspend fun registerTeacher(@Body request: LectorRegisterRequest): Response<RegisterResponse>

    @GET("Student/getById")
    suspend fun getStudentProfile(@Query("id") id: Int): Response<StudentProfile>

    @GET("Lector/getById")
    suspend fun getTeacherProfile(@Query("id") id: Int): Response<LectorProfile>

    @GET("Admin/getById")
    suspend fun getAdminProfile(@Query("id") id: Int): Response<AdminProfile>

    @GET("Student/getStudentPhotoUrls")
    suspend fun getStudentPhotoUrls(@Query("userId") userId: Int): Response<List<FaceImageDto>>

    @GET("Student/getPhoto")
    @Streaming
    suspend fun getPhoto(@Query("imageUrl") imageUrl: String): Response<ResponseBody>

    @Multipart
    @POST("Student/addPhoto")
    suspend fun addPhoto(
        @Part file: MultipartBody.Part
    ): Response<Unit>
    @DELETE("Student/deletePhoto")
    suspend fun deleteStudentPhoto(@Query("photoId") photoId: Int): Response<Unit>

    @GET("Course/getAllGroupCourses")
    suspend fun getCoursesByGroup(@Query("groupId") groupId: Int): Response<List<CourseDto>>

    @GET("Course/getFiltered")
    suspend fun getFilteredCourses(
        @Query("subjectId") subjectId: Int? = null,
        @Query("lectorId") lectorId: Int? = null
    ): Response<List<CourseDto>>

    @GET("Course/getById")
    suspend fun getCourseById(@Query("id") id: Int): Response<CourseDto>

    @GET("Lecture/getFiltered")
    suspend fun getFilteredLectures(
        @Query("courseId") courseId: Int,
        @Query("isAttended") isAttended: Boolean = false,
        @Query("isActual") isActual: Boolean = false
    ): Response<List<LectureDto>>

    @POST("Lecture/create")
    suspend fun createLecture(
        @Query("courseId") courseId: Int,
        @Query("date") date: String
    ): Response<Unit>

    @DELETE("Lecture/delete")
    suspend fun deleteLecture(@Query("id") id: Int): Response<Unit>

    // Лекции
    @GET("Lecture/getById")
    suspend fun getLectureById(@Query("id") id: Int): Response<LectureDto>

    @POST("Lecture/addLecturePhoto")
    @Multipart
    suspend fun addLecturePhoto(
        @Part("LectureId") lectureId: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @POST("Attendance/autoAttendance")
    @FormUrlEncoded
    suspend fun autoAttendance(
        @Field("lectureId") lectureId: Int
    ): Response<List<AttendanceDto>>

    @POST("Attendance/addAttendance")
    suspend fun addAttendance(@Body attendance: List<AttendanceDto>): Response<Unit>

    @GET("Lecture/getLecturePhoto")
    suspend fun getLecturePhoto(@Query("lectureId") lectureId: Int): Response<ResponseBody>

    @GET("Attendance/getAttendanceLecture")
    suspend fun getAttendanceLecture(@Query("lectureId") lectureId: Int): Response<List<AttendanceDto>>

    @POST("Attendance/updateAttendance")
    suspend fun updateAttendance(@Body attendance: List<AttendanceDto>): Response<Unit>

    @GET("Attendance/getAttendanceByCourse")
    suspend fun getAttendanceByCourse(@Query("courseId") courseId: Int): Response<Map<String, List<AttendanceDto>>>

    @GET("Attendance/getGroupAttendanceByCourse")
    suspend fun getGroupAttendanceByCourse(
        @Query("groupId") groupId: Int,
        @Query("courseId") courseId: Int
    ): Response<Map<String, List<AttendanceDto>>>

    @GET("Student/getStudentsByCourse")
    suspend fun getStudentsByCourse(@Query("courseId") courseId: Int): Response<List<StudentProfile>>

    @GET("Subject/getFiltered")
    suspend fun getFilteredSubjects(@Query("name") name: String? = null): Response<List<SubjectDto>>

    @POST("Subject/create")
    suspend fun createSubject(@Query("name") name: String): Response<Unit>

    @POST("Subject/delete")
    suspend fun deleteSubject(@Query("id") id: Int): Response<Unit>

    @GET("Group/getFiltered")
    suspend fun getFilteredGroups(
        @Query("number") number: Int? = null,
        @Query("year") year: Int? = null
    ): Response<List<GroupDto>>

    @POST("Group/create")
    suspend fun createGroup(
        @Query("number") number: Int,
        @Query("year") year: Int,
        @Query("createDate") createDate: String
    ): Response<Unit>

    @POST("Group/delete")
    suspend fun deleteGroup(@Query("id") id: Int): Response<Unit>

    @GET("Student/getFiltered")
    suspend fun getFilteredStudents(
        @Query("name") name: String? = null,
        @Query("surname") surname: String? = null,
        @Query("patronymic") patronymic: String? = null,
        @Query("email") email: String? = null,
        @Query("groupId") groupId: Int? = null,
        @Query("studentIdNumber") studentIdNumber: String? = null
    ): Response<List<StudentProfile>>

    @POST("Student/setGroup")
    suspend fun setStudentGroup(
        @Query("studentId") studentId: Int,
        @Query("groupId") groupId: Int
    ): Response<Unit>

    @POST("Course/create")
    suspend fun createCourse(
        @Query("SubjectId") subjectId: Int,
        @Query("LectorId") lectorId: Int,
        @Query("StartDate") startDate: String,
        @Query("EndDate") endDate: String
    ): Response<Unit>

    @GET("Course/getAllCourseGroups")
    suspend fun getCourseGroups(@Query("courseId") courseId: Int): Response<List<GroupDto>>

    @POST("Course/removeGroup")
    suspend fun removeGroupFromCourse(
        @Query("courseId") courseId: Int,
        @Query("groupId") groupId: Int
    ): Response<Unit>

    @POST("Course/addGroup")
    suspend fun addGroupToCourse(
        @Query("courseId") courseId: Int,
        @Query("groupId") groupId: Int
    ): Response<Unit>

    @POST("Course/delete")
    suspend fun deleteCourse(
        @Query("id") id: Int,
    ): Response<Unit>

    @GET("Lector/getFiltered")
    suspend fun getFilteredLectors(
        @Query("Name") name: String? = null,
        @Query("Surname") surname: String? = null,
        @Query("Patronymic") patronymic: String? = null,
        @Query("Email") email: String? = null,
        @Query("Department") department: String? = null
    ): Response<List<LectorProfile>>
}
