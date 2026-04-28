using System.Security.Claims;
using AutoMapper;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.Attendance.Contract;
using Diplom.Validators.Attendance;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;

namespace Diplom.Controllers.Attendance;

[ApiController]
[Route("[controller]")]
public class AttendanceController : ControllerBase
{
    private readonly IAttendanceService _attendanceService;
    private readonly ILectureService _lectureService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public AttendanceController(
        IAttendanceService attendanceService,
        ILectureService lectureService,
        IMapper mapper,
        ILogger logger)
    {
        _attendanceService = attendanceService;
        _lectureService = lectureService;
        _mapper = mapper;
        _logger = logger;
    }
    
   /*[HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            return Ok(_mapper.Map<GroupDto>(await _attendanceService.GetById(id)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }*/
    
    [HttpGet]
    [Route("getAttendanceByCourse")]
    public async Task<IActionResult> GetAttendanceByCourse([FromQuery] int courseId)
    {
        try
        {
            return Ok(_mapper.Map<IDictionary<string,IList<AttendanceDto>>>(await _attendanceService.GetAttendanceStudentsByCourse(courseId)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getGroupAttendanceByCourse")]
    public async Task<IActionResult> GetGroupAttendanceByCourse([FromQuery] int groupId, int courseId)
    {
        try
        {
            return Ok(_mapper.Map<IDictionary<string,IList<AttendanceDto>>>(await _attendanceService.GetGroupAttendanceByCourse(groupId,courseId)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getAttendanceLecture")]
    public async Task<IActionResult> GetAttendanceLecture([FromQuery] int lectureId)
    {
        try
        {
            return Ok(_mapper.Map<IList<AttendanceDto>>(await _attendanceService.GetAttendanceLecture(lectureId)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Lector")]
    [Route("autoAttendance")]
    public async Task<IActionResult> AutoAttendance([FromForm] int lectureId)
    {
        try
        {
            var (photo,type) = await _lectureService.GetLectureAttendancePhoto(lectureId);
            if (photo == null)
            {
                _logger.Warning("Stream is null");
                return NotFound();
            }
            if (photo.Length == 0)
            {
                _logger.Warning("Zero bytes read");
                return NotFound();
            }
            return Ok(_mapper.Map<IList<AttendanceDto>>(await _attendanceService.AutoSetAttendanceLecture(lectureId,photo)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Lector")]
    [Route("addAttendance")]
    public async Task<IActionResult> AddAttendance([FromBody] IList<AttendanceDto> attendance)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            await _attendanceService.AddAttendanceLecture(_mapper.Map<List<AttendanceModel>>(attendance),int.Parse(userId));
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpPost]
    [Authorize(Roles = "Lector")]
    [Route("updateAttendance")]
    public async Task<IActionResult> UpdateAttendance([FromBody] IList<AttendanceDto> attendance)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            await _attendanceService.UpdateAttendanceLecture(_mapper.Map<List<AttendanceModel>>(attendance),int.Parse(userId));
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}