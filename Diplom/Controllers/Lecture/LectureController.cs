using System.Security.Claims;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.Attendance.Contract;
using Diplom.Controllers.Lecture.Contract;
using Diplom.Controllers.Subject.Contract;
using Diplom.Validators.Attendance;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;
namespace Diplom.Controllers.Lecture;


[ApiController]
[Route("[controller]")]
public class LectureController : ControllerBase
{ 
    private readonly ILectureService _lectureService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public LectureController(ILectureService lectureService, IMapper mapper, ILogger logger)
    {
        _lectureService = lectureService;
        _mapper = mapper;
        _logger = logger;
    }
    
    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            return Ok(_mapper.Map<LectureDto>(await _lectureService.GetById(id)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Authorize]
    [Route("getFiltered")]
    public async Task<IActionResult> GetAllLecturesByCourse([FromQuery] LectureFilter filter)
    {
        try
        {
            return Ok(_mapper.Map<IEnumerable<LectureDto>>(await _lectureService.GetFiltered(filter)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Lector")]
    [Route("addLecturePhoto")]
    public async Task<IActionResult> AutoAttendance([FromForm] AddLecturePhotoRequest request)
    {
        var validatioResult = new AutoAttendanceValidator().Validate(request);
        if (!validatioResult.IsValid)
        {
            _logger.Error(validatioResult.ToString());
            return BadRequest(validatioResult.ToString());
        }
        try
        {
            await _lectureService.AddLecturePhoto(request.LectureId, request.File);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet("getLecturePhoto")]
    [ProducesResponseType(StatusCodes.Status200OK, Type = typeof(FileResult))]
    [Produces("image/jpeg")]
    public async Task<IActionResult> GetPhoto([FromQuery] int lectureId)
    {
        try
        {
            var (stream,type) = await _lectureService.GetLectureAttendancePhoto(lectureId);
            if (stream == null)
            {
                _logger.Warning("Stream is null");
                return NotFound();
            }
            using var memoryStream = new MemoryStream();
            await stream.CopyToAsync(memoryStream);
            var imageBytes = memoryStream.ToArray();
            if (imageBytes.Length == 0)
            {
                _logger.Warning("Zero bytes read");
                return NotFound();
            }
            return File(imageBytes, type);
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    
    [HttpPost]
    [Authorize(Roles = "Lector")]
    [Route("create")]
    public async Task<IActionResult> Create([FromQuery] LectureCreateRequest request)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var lecture = _mapper.Map<LectureModel>(request);
            await _lectureService.Create(lecture,int.Parse(userId));
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpDelete]
    [Authorize(Roles = "Lector")]
    [Route("delete")]
    public async Task<IActionResult> Delete([FromQuery] int id)
    {
        try
        {
            await _lectureService.Delete(id);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}