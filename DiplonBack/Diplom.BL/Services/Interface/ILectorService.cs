using Diplom.BL.Filters;
using Diplom.BL.Models;
using Microsoft.AspNetCore.Identity;

namespace Diplom.BL.Services.Interface;

public interface ILectorService
{
    Task<IEnumerable<LectorModel>> GetAll();
    Task<LectorModel?> GetById(int lectorId);
    Task<IEnumerable<LectorModel>> GetFiltered(LectorFilter filter);
    Task<IdentityResult> Create(LectorModel lector,string password);
    Task<IdentityResult> Update(LectorModel lector,string password);
}