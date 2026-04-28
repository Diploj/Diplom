using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.Course.Contract;
using Diplom.Controllers.Group.Contract;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;

namespace Diplom.Controllers.Course;
[ApiController]
[Route("[controller]")]
public class CourseController : ControllerBase
{
    private readonly ICourseService _courseService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public CourseController(ICourseService courseService, IMapper mapper, ILogger logger)
    {
        _courseService = courseService;
        _mapper = mapper;
        _logger = logger;
    }
    
    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            return Ok(_mapper.Map<CourseDto>(await _courseService.GetById(id)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getFiltered")]
    public async Task<IActionResult> GetFiltered([FromQuery] CourseFilter filter)
    {
        try
        {
            return Ok(_mapper.Map<IEnumerable<CourseDto>>(await _courseService.GetFiltered(filter)));
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
    public async Task<IActionResult> Create([FromQuery] CourseCreateRequest request)
    {
        try
        {
            var course = _mapper.Map<CourseModel>(request);
            await _courseService.Create(course);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Authorize]
    [Route("getAllGroupCourses")]
    public async Task<IActionResult> GetAllGroupCourses([FromQuery] int groupId)
    {
        try
        {
            var course =  await _courseService.GetAllGroupCourses(groupId);
            return Ok(_mapper.Map<IEnumerable<CourseDto>>(course));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Authorize]
    [Route("getAllCourseGroups")]
    public async Task<IActionResult> GetAllCourseGroups([FromQuery] int courseId)
    {
        try
        {
            var groups =  await _courseService.GetAllCourseGroups(courseId);
            return Ok(_mapper.Map<IEnumerable<GroupDto>>(groups));
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
            await _courseService.Delete(id);
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
    [Route("addGroup")]
    public async Task<IActionResult> AddGroup([FromQuery] int courseId, int groupId)
    {
        try
        {
            await _courseService.SubscribeGroupOnCourse(courseId, groupId);
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
    [Route("removeGroup")]
    public async Task<IActionResult> RemoveGroup([FromQuery] int courseId, int groupId)
    {
        try
        {
            await _courseService.UnSubscribeGroupOnCourse(courseId, groupId);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}