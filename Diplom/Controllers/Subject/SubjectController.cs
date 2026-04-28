using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.Subject.Contract;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;
namespace Diplom.Controllers.Subject;

[ApiController]
[Route("[controller]")]
public class SubjectController : ControllerBase
{
    private readonly ISubjectService _subjectService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public SubjectController(ISubjectService subjectService, IMapper mapper, ILogger logger)
    {
        _subjectService = subjectService;
        _mapper = mapper;
        _logger = logger;
    }
    
    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            return Ok(_mapper.Map<SubjectDto>(await _subjectService.GetById(id)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getFiltered")]
    public async Task<IActionResult> GetFiltered([FromQuery] SubjectFilter filter)
    {
        try
        {
            return Ok(_mapper.Map<IEnumerable<SubjectDto>>(await _subjectService.GetFiltered(filter)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Admin")]
    [Route("create")]
    public async Task<IActionResult> Create([FromQuery] SubjectCreateRequest request)
    {
        try
        {
            var subject = _mapper.Map<SubjectModel>(request);
            await _subjectService.Create(subject);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Admin")]
    [Route("update")]
    public async Task<IActionResult> Update([FromQuery] SubjectUpdateRequest request)
    {
        try
        {
            var subject = _mapper.Map<SubjectModel>(request);
            await _subjectService.Update(subject);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Admin")]
    [Route("delete")]
    public async Task<IActionResult> Delete([FromQuery] int id)
    {
        try
        {
            await _subjectService.Delete(id);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}