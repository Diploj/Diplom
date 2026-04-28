using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.Group.Contract;

namespace Diplom.Mapper;

public class GroupProfile : Profile
{
    public GroupProfile()
    {
        CreateMap<GroupCreateRequest, GroupModel>();
        CreateMap<GroupUpdateRequest, GroupModel>();
        CreateMap<GroupModel, GroupDto>();
    }
}