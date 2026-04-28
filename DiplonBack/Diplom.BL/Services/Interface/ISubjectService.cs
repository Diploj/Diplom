using Diplom.BL.Filters;
using Diplom.BL.Models;

namespace Diplom.BL.Services.Interface;

public interface ISubjectService
{
    Task<IEnumerable<SubjectModel>> GetAll();
    Task<IEnumerable<SubjectModel>> GetFiltered(SubjectFilter filter);
    Task<SubjectModel?> GetById(int subjectId);
    Task Create(SubjectModel model);
    Task Update(SubjectModel model);
    Task Delete(int subjectId);
}