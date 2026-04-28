using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Services.Interface;

public interface ICourseService
{
    Task<IEnumerable<CourseModel>> GetAll();
    Task<IEnumerable<CourseModel>> GetFiltered(CourseFilter filter);
    Task<IEnumerable<CourseModel>> GetAllGroupCourses(int groupId);
    Task<IEnumerable<GroupModel>> GetAllCourseGroups(int courseId);
    Task<CourseModel?> GetById(int courseId);
    Task Create(CourseModel model);
    Task Update(CourseModel model);
    Task Delete(int courseId);
    Task SubscribeGroupOnCourse(int courseId,int groupId);
    Task UnSubscribeGroupOnCourse(int courseId,int groupId);
}