using AutoMapper;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.User.Contract;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;
namespace Diplom.Controllers.User;

[ApiController]
[Route("[controller]")]
public class AdminController : ControllerBase
{
    private readonly IUserService _userService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public AdminController(IUserService userService, IMapper mapper, ILogger logger)
    {
        _userService = userService;
        _mapper = mapper;
        _logger = logger;
    }

    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            var userModel = await _userService.GetById(id);
            if (userModel == null)
            {
                return NotFound();
            }
            return Ok(_mapper.Map<UserDto>(userModel));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}