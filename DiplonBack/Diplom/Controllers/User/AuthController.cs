using System.IdentityModel.Tokens.Jwt;
using AutoMapper;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.BL.Utils;
using Diplom.Controllers.User.Contract;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;


namespace Diplom.Controllers.User;

[ApiController]
[Route("[controller]")]
public class AuthController : ControllerBase
{
    private readonly IUserService _userService;
    private readonly IStudentService _studentService;
    private readonly ILectorService _lectorService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger ;
    private readonly ITokenGenerator _tokenGenerator;


    public AuthController(IUserService userService, IStudentService studentService, ILectorService lectorService, IMapper mapper, ILogger logger, ITokenGenerator tokenGenerator)
    {
        _userService = userService;
        _studentService = studentService;
        _lectorService = lectorService;
        _mapper = mapper;
        _logger = logger;
        _tokenGenerator = tokenGenerator;
    }

    [HttpPost("register/user")]
    public async Task<IActionResult> RegisterUser([FromBody] UserCreateRequest request)
    {
        var user = _mapper.Map<UserModel>(request);
        var result = await _userService.Create(user, request.Password);
        if (result.Succeeded)
        {
            return  Ok(new { Message = "User registered successfully" });
        }
        return BadRequest(result.Errors);
    }
    
    [HttpPost("register/student")]
    public async Task<IActionResult> RegisterStudent([FromBody] StudentCreateRequest request)
    {
        var studentModel = _mapper.Map<StudentModel>(request);
        var result = await _studentService.Create(studentModel, request.Password);
        if (result.Succeeded)
        {
            return  Ok(new { Message = "Student registered successfully" });
        }
        return BadRequest(result.Errors);
    }
    [HttpPost("register/lector")]
    public async Task<IActionResult> RegisterLector([FromBody] LectorCreateRequest request)
    {
        var lector = _mapper.Map<LectorModel>(request);
        var result = await _lectorService.Create(lector, request.Password);
        if (result.Succeeded)
        {
            return  Ok(new { Message = "Lector registered successfully" });
        }
        return BadRequest(result.Errors);
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] UserLoginRequest request)
    {
        var user = await _userService.GetByEmail(request.Email);
        if (user != null && await _userService.CheckPassword(user, request.Password))
        {
            var roles = await _userService.GetRole(user);
            var token = _tokenGenerator.Generate(user, roles);

            return Ok(new
            {
                id = user.Id,
                token = new JwtSecurityTokenHandler().WriteToken(token),
                role = roles[0],
                expiration = token.ValidTo
            });
        }
        return BadRequest("Invalid username or password" );
    }
}