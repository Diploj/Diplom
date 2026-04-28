using System.Security.Claims;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.User.Contract;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;
namespace Diplom.Controllers.User;

[ApiController]
[Route("[controller]")]
public class LectorController : ControllerBase
{
    private readonly ILectorService _lectorService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public LectorController(ILectorService lectorService, IMapper mapper, ILogger logger)
    {
        _lectorService = lectorService;
        _mapper = mapper;
        _logger = logger;
    }
    
    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            var studentModel = await _lectorService.GetById(id);
            if (studentModel == null)
            {
                return NotFound();
            }
            return Ok(_mapper.Map<LectorDto>(studentModel));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getFiltered")]
    public async Task<IActionResult> GetFiltered([FromQuery] LectorFilter filter)
    {
        try
        {
            var students = await _lectorService.GetFiltered(filter);
            return Ok(_mapper.Map<IEnumerable<LectorDto>>(students));
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
    public async Task<IActionResult> Update([FromBody] LectorUpdateRequest request)
    {
        try
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (int.Parse(userId) != request.UserId)
            {
                return Forbid();
            }
            var lector =_mapper.Map<LectorModel>(request);
            var result =  await _lectorService.Update(lector,request.Password);
            return Ok(result);
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
}