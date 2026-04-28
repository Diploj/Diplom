using System.Linq.Expressions;
using System.Transactions;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Serilog;

namespace Diplom.BL.Services.Implementation;

public class LectorService : ILectorService
{
    private readonly UserManager<User> _userRepository;
    private readonly IRepository<Lector> _lectorRepository;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public LectorService(
        UserManager<User> userRepository,
        IRepository<Lector> lectorRepository,
        IMapper mapper,
        ILogger logger)
    {
        _userRepository = userRepository;
        _lectorRepository = lectorRepository;
        _mapper = mapper;
        _logger = logger;
    }
    public async Task<IEnumerable<LectorModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<LectorModel>>(await _lectorRepository.GetAllAsync((s => true)));
    }

    public async Task<LectorModel?> GetById(int lectorId)
    {
        return _mapper.Map<LectorModel>(await _lectorRepository.GetByIdAsync(lectorId));
    }

    public async Task<IEnumerable<LectorModel>> GetFiltered(LectorFilter filter)
    {
        Expression<Func<Lector,bool>> expression = s => 
            (filter.Name == null || s.User.Name.Contains(filter.Name)) &&
            (filter.Surname == null || s.User.Surname.Contains(filter.Surname)) &&
            (filter.Email == null || s.User.Email.Contains(filter.Email)) &&
            (filter.Patronymic == null || s.User.Patronymic.Contains(filter.Patronymic)) &&
            (filter.Department == null || s.Department == filter.Department);
        return _mapper.Map<IEnumerable<LectorModel>>(await _lectorRepository.GetAllAsync(expression));
    }

    public async Task<IdentityResult> Create(LectorModel lector, string password)
    {
        try
        {
            using (var scope = new TransactionScope(TransactionScopeAsyncFlowOption.Enabled))
            {
                var userEntity = _mapper.Map<User>(lector);
                var lectorEntity = _mapper.Map<Lector>(lector);
                var result = await _userRepository.CreateAsync(userEntity, password);
                if (result.Succeeded)
                {
                    lectorEntity.UserId = userEntity.Id;
                    await _lectorRepository.AddAsync(lectorEntity);
                    await _userRepository.UpdateSecurityStampAsync(userEntity);
                    await _userRepository.AddToRoleAsync(userEntity, "Lector");
                }
                scope.Complete(); 
                return result;
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IdentityResult> Update(LectorModel lector, string password)
    {
        try
        {
            var userEntity = _mapper.Map<User>(lector);
            var lectorEntity = _mapper.Map<Lector>(lector);
            if (await _lectorRepository.ExistsAsync(lectorEntity.UserId))
            {
                userEntity.PasswordHash = _userRepository.PasswordHasher?.HashPassword(userEntity, password);
                await _lectorRepository.UpdateAsync(lectorEntity);
                var result = await _userRepository.UpdateAsync(userEntity);
                return result;
            }
            _logger.Error($"Lector {userEntity.UserName} with id {userEntity.Id} not found");
            throw new KeyNotFoundException("Lector not found");
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}