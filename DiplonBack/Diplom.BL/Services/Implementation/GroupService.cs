using System.Linq.Expressions;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;
using Serilog;

namespace Diplom.BL.Services.Implementation;

public class GroupService : IGroupService
{
    private readonly IRepository<Group> _groupRepository;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public GroupService(IRepository<Group> groupRepository, IMapper mapper, ILogger logger)
    {
        _groupRepository = groupRepository;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<GroupModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<GroupModel>>(await _groupRepository.GetAllAsync((g => true)));
    }

    public async Task<IEnumerable<GroupModel>> GetFiltered(GroupFilter filter)
    {
        Expression<Func<Group, bool>> expression = g =>
            (filter.Number == null || g.Number == filter.Number) &&
            (filter.Year == null || g.Year == filter.Year);
        return _mapper.Map<IEnumerable<GroupModel>>(await _groupRepository.GetAllAsync(expression));
    }

    public async Task<GroupModel?> GetById(int courseId)
    {
        return _mapper.Map<GroupModel>(await _groupRepository.GetByIdAsync(courseId));
    }

    public async Task Create(GroupModel model)
    {
        try
        {
            var groupEntity = _mapper.Map<Group>(model);
            await _groupRepository.AddAsync(groupEntity);
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Update(GroupModel model)
    {
        try
        {
            var groupEntity = _mapper.Map<Group>(model);
            if (await _groupRepository.ExistsAsync(groupEntity.Id))
            {
                await _groupRepository.UpdateAsync(groupEntity);
            }
            else
            {
                _logger.Warning($"Group with id: {model.Id} was not found");
                throw new KeyNotFoundException("Group not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Delete(int groupId)
    {
        try
        {
            var groupEntity = await _groupRepository.GetByIdAsync(groupId);
            if (groupEntity != null)
            {
                await _groupRepository.DeleteAsync(groupEntity);
            }
            else
            {
                _logger.Warning($"Group with id: {groupId} was not found");
                throw new KeyNotFoundException($"Group not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}