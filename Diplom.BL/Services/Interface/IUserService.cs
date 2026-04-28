using Diplom.BL.Models;
using Microsoft.AspNetCore.Identity;

namespace Diplom.BL.Services.Interface;

public interface IUserService
{
    Task<IEnumerable<UserModel>> GetAll();
    Task<UserModel?> GetById(int userId);
    Task<UserModel?> GetByEmail(string email);
    Task<IList<string>> GetRole(UserModel user);
    Task<bool> CheckPassword(UserModel user, string password);
    Task<IdentityResult> Create(UserModel user,string password);
    //Task<IdentityResult> Update(UserModel user,string password);
    Task<IdentityResult> Delete(int userId);
}