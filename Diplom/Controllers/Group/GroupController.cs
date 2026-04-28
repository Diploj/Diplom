using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Services.Interface;
using Diplom.Controllers.Group.Contract;
using Diplom.Validators.Group;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;
using ILogger = Serilog.ILogger;
namespace Diplom.Controllers.Group;


[ApiController]
[Route("[controller]")]
public class GroupController : ControllerBase
{
    private readonly IGroupService _groupService;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public GroupController(IGroupService groupService, IMapper mapper, ILogger logger)
    {
        _groupService = groupService;
        _mapper = mapper;
        _logger = logger;
    }
    
    [HttpGet]
    [Route("getById")]
    public async Task<IActionResult> GetById([FromQuery] int id)
    {
        try
        {
            return Ok(_mapper.Map<GroupDto>(await _groupService.GetById(id)));
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
    
    [HttpGet]
    [Route("getFiltered")]
    public async Task<IActionResult> GetFiltered([FromQuery] GroupFilter filter)
    {
        try
        {
            return Ok(_mapper.Map<IEnumerable<GroupDto>>(await _groupService.GetFiltered(filter)));
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
    public async Task<IActionResult> Create([FromQuery] GroupCreateRequest request)
    {
        var validatioResult = new GroupCreateValidator().Validate(request);
        if (!validatioResult.IsValid)
        {
            _logger.Error(validatioResult.ToString());
            return BadRequest(validatioResult.ToString());
        }
        try
        {
            var group = _mapper.Map<GroupModel>(request);
            await _groupService.Create(group);
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
    public async Task<IActionResult> Update([FromQuery] GroupUpdateRequest request)
    {
        var validatioResult = new GroupUpdateValidator().Validate(request);
        if (!validatioResult.IsValid)
        {
            _logger.Error(validatioResult.ToString());
            return BadRequest(validatioResult.ToString());
        }
        try
        {
            var group = _mapper.Map<GroupModel>(request);
            await _groupService.Update(group);
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
            await _groupService.Delete(id);
            return Ok();
        }
        catch (Exception e)
        {
            _logger.Error(e.ToString());
            return BadRequest(e.Message); 
        }
    }
}