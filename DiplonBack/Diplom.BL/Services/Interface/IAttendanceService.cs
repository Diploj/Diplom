using Diplom.BL.Models;
using Diplom.Data.Entities;
using Microsoft.AspNetCore.Http;

namespace Diplom.BL.Services.Interface;

public interface IAttendanceService
{
    Task<Dictionary<string,List<AttendanceModel>>> GetGroupAttendanceByCourse(int groupId, int courseId);
    Task<Dictionary<string,List<AttendanceModel>>> GetAttendanceStudentsByCourse(int courseId);
    Task<IList<AttendanceModel>> GetAttendanceLecture(int lectureId);
    Task AddAttendanceLecture(List<AttendanceModel> attendance, int lectorId);
    Task UpdateAttendanceLecture(List<AttendanceModel> attendance,int lectorId);
    Task<List<AttendanceModel>> AutoSetAttendanceLecture(int lectureId, Stream photo);
    
}