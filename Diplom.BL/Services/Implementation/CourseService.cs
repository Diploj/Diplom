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

public class CourseService : ICourseService
{
    private readonly IRepository<Course> _courseRepository;
    private readonly IRepository<Group> _groupRepository;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public CourseService(IRepository<Course> courseRepository, IRepository<Group> groupRepository, IMapper mapper, ILogger logger)
    {
        _courseRepository = courseRepository;
        _groupRepository = groupRepository;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<CourseModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<CourseModel>>(await _courseRepository.GetAllAsync(x => true));
    }

    public async Task<IEnumerable<CourseModel>> GetFiltered(CourseFilter filter)
    {
        Expression<Func<Course, bool>> expression = x => (filter.LectorId == null || filter.LectorId == x.LectorId) &&
                                                         (filter.SubjectId == null || filter.SubjectId == x.SubjectId);
        return _mapper.Map<IEnumerable<CourseModel>>(await _courseRepository.GetAllAsync(expression));
    }

    public async Task<IEnumerable<CourseModel>> GetAllGroupCourses(int groupId)
    {
        var group = await _groupRepository.GetByIdAsync(groupId);
        if (group != null)
        {
            return _mapper.Map<IEnumerable<CourseModel>>(group.Courses);
        }
        _logger.Warning($"Group with id: {groupId} was not found");
        throw new KeyNotFoundException($"Group not found");
    }

    public async Task<IEnumerable<GroupModel>> GetAllCourseGroups(int courseId)
    {
        var course = await _courseRepository.GetByIdAsync(courseId);
        if (course  != null)
        {
            return _mapper.Map<IEnumerable<GroupModel>>(course.Groups);
        }
        _logger.Warning($"Course with id: {courseId} was not found");
        throw new KeyNotFoundException($"Course not found");
    }

    public async Task<CourseModel?> GetById(int courseId)
    {
        return _mapper.Map<CourseModel>(await _courseRepository.GetByIdAsync(courseId));
    }

    public async Task Create(CourseModel model)
    {
        try
        {
            var courseEntity = _mapper.Map<Course>(model);
            await _courseRepository.AddAsync(courseEntity);
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Update(CourseModel model)
    {
        try
        {
            var courseEntity = _mapper.Map<Course>(model);
            if (await _courseRepository.ExistsAsync(courseEntity.Id))
            {
                await _courseRepository.UpdateAsync(courseEntity);
            }
            else
            {
                _logger.Warning($"Course with id: {model.Id} was not found");
                throw new KeyNotFoundException($"Course does not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Delete(int courseId)
    {
        try
        {
            var courseEntity = await _courseRepository.GetByIdAsync(courseId);
            if (courseEntity != null)
            {
                await _courseRepository.DeleteAsync(courseEntity);
            }
            else
            {
                _logger.Warning($"Course with id: {courseId} was not found");
                throw new KeyNotFoundException($"Course does not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task SubscribeGroupOnCourse(int courseId, int groupId)
    {
        try
        {
            var courseEntity = await _courseRepository.GetByIdAsync(courseId);
            var groupEntity = await _groupRepository.GetByIdAsync(groupId);
            if (courseEntity != null && groupEntity != null)
            {
                courseEntity.Groups.Add(groupEntity);
                await _courseRepository.UpdateAsync(courseEntity);
            }
            else
            {
                throw new KeyNotFoundException("Course or Group doesn't exist");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task UnSubscribeGroupOnCourse(int courseId, int groupId)
    {
        try
        {
            var courseEntity = await _courseRepository.GetByIdAsync(courseId);
            var groupEntity = await _groupRepository.GetByIdAsync(groupId);
            if (courseEntity != null && groupEntity != null)
            {
                courseEntity.Groups.Remove(groupEntity);
                await _courseRepository.UpdateAsync(courseEntity);
            }
            else
            {
                throw new KeyNotFoundException("Course or group doesn't exist");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}