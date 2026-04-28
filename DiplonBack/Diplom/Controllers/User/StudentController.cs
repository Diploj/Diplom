using System.Security.Claims;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.User.Contract;
using Diplom.Validators.Student;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;

namespace Diplom.Controllers.User;

[ApiController]
[Route("[controller]")]
public class StudentController : ControllerBase
{
    private readonly IStudentService _studentService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public StudentController(IStudentService studentService, IMapper mapper, ILogger logger)
    {
        _studentService = studentService;
        _mapper = mapper;
        _logger = logger;
    }

    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            var studentModel = await _studentService.GetById(id);
            if (studentModel == null)
            {
                return NotFound();
            }
            return Ok(_mapper.Map<StudentDto>(studentModel));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getFiltered")]
    public async Task<IActionResult> GetFiltered([FromQuery] StudentFilter filter)
    {
        try
        {
            var students = await _studentService.GetFiltered(filter);
            return Ok(_mapper.Map<IEnumerable<StudentModel>>(students));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getStudentsByCourse")]
    public async Task<IActionResult> GetStudentsByCourse([FromQuery] int courseId)
    {
        try
        {
            var students = await _studentService.GetAllStudentsByCourse(courseId);
            return Ok(_mapper.Map<IEnumerable<StudentModel>>(students));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize]
    [Route("update")]
    public async Task<IActionResult> Update([FromBody] StudentUpdateRequest request)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (int.Parse(userId) != request.UserId)
            {
                return Forbid();
            }
            var student =_mapper.Map<StudentModel>(request);
            var result =  await _studentService.Update(student,request.Password);
            return Ok(result);
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Student")]
    [Route("addPhoto")]
    public async Task<IActionResult> AddPhoto([FromForm] AddPhotoRequest request)
    {
        var validatioResult = new AddPhotoValidator().Validate(request);
        if (!validatioResult.IsValid)
        {
            _logger.Error(validatioResult.ToString());
            return BadRequest(validatioResult.ToString());
        }
        try
        {
            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value);
            await _studentService.AddPhoto(userId, request.File);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet("getPhoto")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(FileResult))]
    public async Task<IActionResult> GetPhoto([FromQuery] string imageUrl)
    {
        try
        {
            var (stream, contentType) = await _studentService.GetPhoto(imageUrl);
            if (stream == null)
            {
                _logger.Warning("Stream is null for {ImageUrl}", imageUrl);
                return NotFound();
            }
            using var memoryStream = new MemoryStream();
            await stream.CopyToAsync(memoryStream);
            var imageBytes = memoryStream.ToArray();
            if (imageBytes.Length == 0)
            {
                _logger.Warning("Zero bytes read for {ImageUrl}", imageUrl);
                return NotFound();
            }
            return File(imageBytes, contentType);
        }
        catch (Exception ex)
        {
            _logger.Error(ex, "GetPhoto failed for {ImageUrl}", imageUrl);
            return StatusCode(500, "Internal server error");
        }
    }
    

    [HttpGet]
    [Authorize]
    [Route("getStudentPhotoUrls")]
    public async Task<IActionResult> GetStudentPhotoUrls([FromQuery] int userId)
    {
        try
        {
            return Ok(_mapper.Map<IList<FaceImageDto>>(await _studentService.GetPhotoUrls(userId)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpDelete]
    [Authorize(Roles = "Student")]
    [Route("deletePhoto")]
    public async Task<IActionResult> DeletePhoto([FromQuery] int photoId)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            await _studentService.DeletePhoto(int.Parse(userId), photoId);
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
    [Route("setGroup")]
    public async Task<IActionResult> SetGroup([FromQuery] int studentId,int groupId)
    {
        try
        {
            await _studentService.SetGroup(studentId, groupId);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}
