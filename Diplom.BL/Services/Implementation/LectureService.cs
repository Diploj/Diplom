using System.Collections.Immutable;
using System.Linq.Expressions;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Options;
using Diplom.BL.Services.Interface;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using Serilog;

namespace Diplom.BL.Services.Implementation;

public class LectureService : ILectureService
{
    private readonly IRepository<Lecture> _lectureRepository;
    private readonly IRepository<Course> _courseRepository;
    private readonly IRepository<Group> _groupRepository;
    private readonly IMinIOService _minioService;
    private readonly MinIOOptions _options;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public LectureService(
        IRepository<Lecture> lectureRepository,
        IRepository<Course> courseRepository,
        IRepository<Group> groupRepository,
        IMinIOService minioService,
        IOptions<MinIOOptions> options,
        IMapper mapper,
        ILogger logger)
    {
        _lectureRepository = lectureRepository;
        _courseRepository = courseRepository;
        _groupRepository = groupRepository;
        _minioService = minioService;
        _options = options.Value;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<LectureModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<LectureModel>>(await _lectureRepository.GetAllAsync(l => true));
    }

    public async Task<IEnumerable<LectureModel>> GetFiltered(LectureFilter filter)
    {
        Func<Lecture, bool> expression = l => 
            (!filter.IsActual || l.Date.Date >= DateTime.Now.Date) &&
            (filter.CourseId == null || l.CourseId == filter.CourseId) &&
            (!filter.IsAttended || l.IsAttended);
        if (filter.GroupId != null)
        {
            var group = await _groupRepository.GetByIdAsync(filter.GroupId.Value);
            if (group != null)
            {
                var lectures = _mapper.Map<List<LectureModel>>(group.Lectures);
                lectures.Sort((l1, l2) => l1.Date.CompareTo(l2.Date));
                return lectures;
            }
            _logger.Warning($"Group with id: {filter.GroupId} was not found");
            throw new KeyNotFoundException($"Group not found");
        }
        else
        {
            var lectures = _mapper.Map<List<LectureModel>>(await _lectureRepository
                    .GetAllAsync(x=> (filter.CourseId == null || x.CourseId == filter.CourseId) &&
                                     (!filter.IsAttended || x.IsAttended)))
                .Where(l => !filter.IsActual || l.Date >= DateTime.Now.Date).ToList();
            lectures.Sort((l1, l2) => l1.Date.CompareTo(l2.Date));                
            return lectures;
        }
    }
    
    public async Task<LectureModel?> GetById(int lectureId)
    {
        return _mapper.Map<LectureModel>(await _lectureRepository.GetByIdAsync(lectureId));
    }

    public async Task Create(LectureModel model,int lectorId)
    {
        try
        {
            var lectureEntity = _mapper.Map<Lecture>(model);
            var course = await _courseRepository.GetByIdAsync(model.CourseId);
            if (course != null)
            {
                if (course.LectorId == lectorId)
                {
                    foreach (var group in course.Groups)
                    {
                        lectureEntity.Groups.Add(group);
                    }
                    await _lectureRepository.AddAsync(lectureEntity);
                }
                else
                {
                    _logger.Warning($"Cannot add lecture on  someone else's course {lectorId}");
                    throw new UnauthorizedAccessException("Cannot add lecture on  someone else's course");
                }
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

    public async Task Update(LectureModel model,int lectorId)
    {
        try
        {
            var lectureEntity = await _lectureRepository.GetByIdAsync(model.Id.Value);
            if (lectureEntity != null)
            {
                var course = await _courseRepository.GetByIdAsync(model.CourseId);
                if (course.LectorId == lectorId)
                {
                    lectureEntity.Date = model.Date;
                    await _lectureRepository.UpdateAsync(lectureEntity);
                    await _lectureRepository.AddAsync(lectureEntity);
                }
                else
                {
                    _logger.Warning($"Cannot update lecture on someone else's course {lectorId}");
                    throw new UnauthorizedAccessException("Cannot update lecture on  someone else's course");
                }
            }
            else
            {
                _logger.Warning($"Lecture with id: {model.Id.Value} was not found");
                throw new KeyNotFoundException($"Lecture not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task AddLecturePhoto(int lectureId, IFormFile photo)
    {
        try
        {
            var lectureEntity = await _lectureRepository.GetByIdAsync(lectureId);
            if (lectureEntity != null)
            {
                await _minioService.UploadLecturePhotoAsync(lectureId, photo.OpenReadStream(), photo.ContentType);
                lectureEntity.IsPhotoLoaded = true;
                lectureEntity.IsAttended = false;
                lectureEntity.Attendances.Clear();
                await _lectureRepository.UpdateAsync(lectureEntity);
            }
            else
            {
                _logger.Warning($"Lecture {lectureId} not found");
                throw new KeyNotFoundException($"Lecture not found");
            }
        }
        catch (Exception e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
    
    public async Task<(Stream,string)> GetLectureAttendancePhoto(int lectureId)
    {
        try
        {
            var lecture = await _lectureRepository.GetByIdAsync(lectureId);
            if (lecture == null)
            {
                _logger.Warning($"Lecture {lectureId} not found");
                throw new KeyNotFoundException("Lecture not found");
            }
            if (!lecture.IsPhotoLoaded)
            {
                _logger.Warning($"Photo {lectureId} not loaded");
                throw new KeyNotFoundException($"Photo not loaded");
            }
            return await _minioService.GetFileAsync(_options.BucketLecturesPhoto, lectureId.ToString());

        }
        catch (Exception e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Delete(int lectureId)
    {
        try
        {
            var lectureEntity = await _lectureRepository.GetByIdAsync(lectureId);
            if (lectureEntity != null)
            {
                await _lectureRepository.DeleteAsync(lectureEntity);
            }
            else
            {
                _logger.Warning($"Lecture with id: {lectureId} was not found");
                throw new KeyNotFoundException($"Lecture not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}