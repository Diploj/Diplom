
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Microsoft.AspNetCore.Http;

namespace Diplom.BL.Services.Interface;

public interface ILectureService
{
    Task<IEnumerable<LectureModel>> GetAll();
    Task<IEnumerable<LectureModel>> GetFiltered(LectureFilter filter);
    Task<LectureModel?> GetById(int lectureId);
    Task Create(LectureModel model,int lectorId);
    Task Update(LectureModel model,int lectorId);
    Task AddLecturePhoto(int lectureId, IFormFile file);
    Task<(Stream,string)> GetLectureAttendancePhoto(int lectureId);
    Task Delete(int lectureId);
}