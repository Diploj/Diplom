using Diplom.BL.Filters;
using Diplom.BL.Models;

namespace Diplom.BL.Services.Interface;

public interface IGroupService
{
    Task<IEnumerable<GroupModel>> GetAll();
    Task<IEnumerable<GroupModel>> GetFiltered(GroupFilter filter);
    Task<GroupModel?> GetById(int groupId);
    Task Create(GroupModel model);
    Task Update(GroupModel model);
    Task Delete(int groupId);
}