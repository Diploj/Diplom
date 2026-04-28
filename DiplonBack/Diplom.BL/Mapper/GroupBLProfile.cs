using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class GroupBLProfile : Profile
{
    public GroupBLProfile()
    {
        CreateMap<Group, GroupModel>();
        CreateMap<GroupModel, Group>();
    }
}