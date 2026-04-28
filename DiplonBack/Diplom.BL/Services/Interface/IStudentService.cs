using Diplom.BL.Filters;
using Diplom.BL.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;

namespace Diplom.BL.Services.Interface;

public interface IStudentService
{
    Task<IEnumerable<StudentModel>> GetAll();
    Task<IEnumerable<StudentModel>> GetFiltered(StudentFilter filter);
    Task<IEnumerable<StudentModel>> GetAllStudentsByCourse(int courseId);
    Task<StudentModel?> GetById(int studentId);
    Task<IdentityResult> Create(StudentModel student,string password);
    Task<IdentityResult> Update(StudentModel student,string password);
    Task SetGroup(int studentId,int groupId);
    Task AddPhoto(int studentId, IFormFile photo);
    Task<IList<FaceImageModel>> GetPhotoUrls(int studentId);
    Task<(Stream,string)> GetPhoto(string imageUrl);
    Task DeletePhoto(int studentId, int photoId);
}