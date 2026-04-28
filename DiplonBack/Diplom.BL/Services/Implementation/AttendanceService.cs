using System.Data.Common;
using AutoMapper;
using Diplom.BL.Models;
using Diplom.BL.Options;
using Diplom.BL.Services.Interface;
using Diplom.Client.Exceptions;
using Diplom.Client.Services;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;
using Serilog;
using Microsoft.Extensions.Options;

namespace Diplom.BL.Services.Implementation;

public class AttendanceService : IAttendanceService
{
    private readonly IRepository<Student> _studentRepository;
    private readonly IRepository<Course> _courseRepository;
    private readonly IRepository<Lecture> _lectureRepository;
    private readonly IRepository<Attendance> _attendanceRepository;
    private readonly IFaceEmbeddingRepository _faceEmbeddingRepository;
    private readonly IRecognitionClient _recognitionClient;
    private readonly DistanceOptions _distanceOptions;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;


    public AttendanceService(
        IRepository<Student> studentRepository,
        IRepository<Course> courseRepository,
        IRepository<Lecture> lectureRepository,
        IFaceEmbeddingRepository faceEmbeddingRepository,
        IRepository<Attendance> attendanceRepository,
        IRecognitionClient recognitionClient,
        IOptions<DistanceOptions> distanceOptions,
        IMapper mapper,
        ILogger logger)
    {
        _studentRepository = studentRepository;
        _courseRepository = courseRepository;
        _lectureRepository = lectureRepository;
        _faceEmbeddingRepository = faceEmbeddingRepository;
        _attendanceRepository = attendanceRepository;
        _recognitionClient = recognitionClient;
        _distanceOptions = distanceOptions.Value;
        _mapper = mapper;
        _logger = logger;
    }


    public async Task<Dictionary<string, List<AttendanceModel>>> GetGroupAttendanceByCourse(int groupId, int courseId)
    {
        try
        {
            var students =  await _studentRepository.GetAllAsync(x => x.GroupId == groupId);
            var lectures = await _lectureRepository.GetAllAsync(x => x.CourseId == courseId);
            var lecturesIds = lectures.Select(x => x.Id).ToList();
            var listAttendance = (await _attendanceRepository
                .GetAllAsync(x => lecturesIds.Contains(x.LectureId)))
                .ToList();
            Dictionary<string, List<AttendanceModel>> result = new Dictionary<string, List<AttendanceModel>>();
            foreach (var student in students)
            {
                var studentAttendance = listAttendance
                    .Where(x => x.StudentId == student.UserId)
                    .ToList();
                var studentName = $"{student.User.Name} {student.User.Surname} {student.User.Patronymic}";
                result.Add(studentName, _mapper.Map<List<AttendanceModel>>(studentAttendance));
            }
            return result;
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
    

    public async Task<Dictionary<string, List<AttendanceModel>>> GetAttendanceStudentsByCourse(int courseId)
    {
        try
        {
            var course = await _courseRepository.GetByIdAsync(courseId);
            var lecturesIds = course.Lectures.Select(x => x.Id).ToList();
            var groupIds = course.Groups.Select(x => x.Id).ToList();
            var listAttendance = (await _attendanceRepository
                .GetAllAsync(x => lecturesIds.Contains(x.LectureId))).ToList();
            var students = await _studentRepository.GetAllAsync(x => x.GroupId!= null && groupIds.Contains(x.GroupId.Value));
            Dictionary<string, List<AttendanceModel>> result = new Dictionary<string, List<AttendanceModel>>();
            foreach (var student in students)
            {
                var studentAttendance = listAttendance
                    .Where(x => x.StudentId == student.UserId)
                    .ToList();
                var studentName = $"{student.User.Name} {student.User.Surname} {student.User.Patronymic}";
                result.Add(studentName, _mapper.Map<List<AttendanceModel>>(studentAttendance));
            }
            return result;
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IList<AttendanceModel>> GetAttendanceLecture(int lectureId)
    {
        try
        {
            var attendance = await _attendanceRepository
                .GetAllAsync(x => x.LectureId == lectureId);
            return _mapper.Map<List<AttendanceModel>>(attendance);
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task AddAttendanceLecture(List<AttendanceModel> attendance, int lectorId)
    {
        try
        {
            var uniqueLectureId = attendance.Select(a => a.LectureId).ToHashSet();
            var lectures = await _lectureRepository.GetAllAsync(l => uniqueLectureId.Contains(l.Id));
            var lectorsIds = lectures.Select(x => x.Course.LectorId).ToHashSet();
            if (lectorsIds.Count == 1 && lectorsIds.Contains(lectorId))
            {
                foreach (var lecture in lectures)
                {
                    lecture.IsAttended = true;
                    lecture.Attendances.Clear();
                }
                await _lectureRepository.UpdateRangeAsync(lectures);
                await _attendanceRepository.AddRangeAsync(_mapper.Map<IEnumerable<Attendance>>(attendance));
            }
            else
            {
                _logger.Warning($"Cannot add attendance to someone else's lecture {lectorId}");
                throw new UnauthorizedAccessException("Cannot add attendance to someone else's lecture");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
    
    public async Task UpdateAttendanceLecture(List<AttendanceModel> attendance, int lectorId)
    {
        try
        {
            var uniqueLectureId = attendance.Select(a => a.LectureId).ToHashSet();
            var lectures = await _lectureRepository.GetAllAsync(l => uniqueLectureId.Contains(l.Id));
            var lectorsIds = lectures.Select(x => x.Course.LectorId).ToHashSet();
            if (lectorsIds.Count == 1 && lectorsIds.Contains(lectorId))
            {
                await _attendanceRepository.UpdateRangeAsync(_mapper.Map<IEnumerable<Attendance>>(attendance));
            }
            else
            {
                _logger.Warning($"Cannot add attendance to someone else's lecture {lectorId}");
                throw new UnauthorizedAccessException("Cannot add attendance to someone else's lecture");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<List<AttendanceModel>> AutoSetAttendanceLecture(int lectureId, Stream photo)
    {
        try
        {
            var lectureEntity = await _lectureRepository.GetByIdAsync(lectureId);
            var courseEntity = await _courseRepository.GetByIdAsync(lectureEntity.CourseId);
            var allStudents = courseEntity.Groups
                .SelectMany(g => g.Students)
                .Select(s => s.UserId)
                .ToList();
            var embeddings = await _recognitionClient.ExtractEmbeddingsAsync(photo);
            var presentStudents = new List<int>();
            foreach (var embedding in embeddings)
            {
                var studentIds = allStudents.ToArray();
                var result = await _faceEmbeddingRepository
                    .GetNearestStudentAsync(embedding, studentIds );
                _logger.Information($"{result.Distance}");
                if (result != default && result.Distance < _distanceOptions.Distance)
                {
                    presentStudents.Add(result.StudentId);
                }
            }
            List<AttendanceModel> attendance = new List<AttendanceModel>();
            foreach (var student in allStudents)
            {
                attendance.Add(new AttendanceModel()
                {
                    StudentId = student,
                    LectureId = lectureId,
                    Attended = presentStudents.Contains(student),
                });
            }
            return attendance;
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
        catch (ClientException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
    
}