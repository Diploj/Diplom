using System.Linq.Expressions;
using System.Runtime.InteropServices.JavaScript;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;
using Serilog;

namespace Diplom.BL.Services.Implementation;

public class SubjectService : ISubjectService
{
    private readonly IRepository<Subject> _subjectRepository;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public SubjectService(IRepository<Subject> subjectRepository, IMapper mapper, ILogger logger)
    {
        _subjectRepository = subjectRepository;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<SubjectModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<SubjectModel>>(await _subjectRepository.GetAllAsync(s => true));
    }

    public async Task<IEnumerable<SubjectModel>> GetFiltered(SubjectFilter filter)
    {
        Expression<Func<Subject,bool>> expression = s => filter.Name == null || s.Name.Contains(filter.Name);
        return _mapper.Map<IEnumerable<SubjectModel>>(await _subjectRepository.GetAllAsync(expression));
    }

    public async Task<SubjectModel?> GetById(int subjectId)
    {
        return _mapper.Map<SubjectModel>(await _subjectRepository.GetByIdAsync(subjectId));
    }

    public async Task Create(SubjectModel model)
    {
        try
        {
            var subjectEntity = _mapper.Map<Subject>(model);
            await _subjectRepository.AddAsync(subjectEntity);
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Update(SubjectModel model)
    {
        try
        {
            var subjectEntity = _mapper.Map<Subject>(model);
            if (await _subjectRepository.ExistsAsync(subjectEntity.Id))
            {
                await _subjectRepository.UpdateAsync(subjectEntity);
            }
            else
            {
                _logger.Warning($"Subject with id: {model.Id} was not found");
                throw new KeyNotFoundException("Subject not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task Delete(int subjectId)
    {
        try
        {
            var subjectEntity = await _subjectRepository.GetByIdAsync(subjectId);
            if (subjectEntity != null)
            {
                await _subjectRepository.DeleteAsync(subjectEntity);
            }
            else
            {
                _logger.Warning($"Subject with id: {subjectId} was not found");
                throw new KeyNotFoundException("Subject not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}