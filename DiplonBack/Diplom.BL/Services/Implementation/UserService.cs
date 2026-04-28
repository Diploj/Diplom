using System.Data.Common;
using AutoMapper;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Client.Exceptions;
using Diplom.Data.Entities;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Serilog;
namespace Diplom.BL.Services.Implementation;

public class UserService : IUserService
{
    private readonly UserManager<User> _userRepository;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public UserService(UserManager<User> userRepository, IMapper mapper, ILogger logger)
    {
        _userRepository = userRepository;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<UserModel>> GetAll()
    {
        return  _mapper.Map<IEnumerable<UserModel>>(await _userRepository.Users.ToListAsync());
    }

    public async Task<UserModel?> GetById(int userId)
    {
        try
        {
            var user = await _userRepository.FindByIdAsync(userId.ToString());
            if (user == null)
                throw new KeyNotFoundException("Пользователь не найден");
            return _mapper.Map<UserModel>(user);
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<UserModel?> GetByEmail(string email)
    {
        try
        {
            var user = await _userRepository.FindByEmailAsync(email);
            if (user == null)
                throw new KeyNotFoundException("Пользователь не найден");
            return _mapper.Map<UserModel>(user);
        }
        catch (DbException  e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IList<string>> GetRole(UserModel user)
    {
        try
        {
            var role = await _userRepository.GetRolesAsync(_mapper.Map<User>(user));
            return role;
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<bool> CheckPassword(UserModel user, string password)
    {
        try
        {
            return await _userRepository.CheckPasswordAsync(_mapper.Map<User>(user), password);
        }
        catch (DbException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IdentityResult> Create(UserModel user, string password)
    {
        try
        {
            var userEntity = _mapper.Map<User>(user);
            var result = await _userRepository.CreateAsync(userEntity, password);
            await _userRepository.UpdateSecurityStampAsync(userEntity);
            await _userRepository.AddToRoleAsync(userEntity, "Admin");
            return result;
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IdentityResult> Update(UserModel user, string password)
    {
        try
        {
            var userEntity = _mapper.Map<User>(user);
            userEntity.PasswordHash = _userRepository.PasswordHasher?.HashPassword(userEntity, password);
            var result = await _userRepository.UpdateAsync(userEntity);
            return result;
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IdentityResult> Delete(int userId)
    {
        try
        {
            var user = await _userRepository.FindByIdAsync(userId.ToString()); 
            if (user == null)
                throw new KeyNotFoundException("Пользователь не найден");
            var result = await _userRepository.DeleteAsync(user);
            return result;
        }
        catch (DbUpdateException e)
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
