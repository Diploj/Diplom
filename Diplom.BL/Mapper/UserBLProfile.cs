using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class UserBLProfile : Profile
{
    public UserBLProfile()
    {
        CreateMap<UserModel, User>()
            .ForMember(dest => dest.UserName, opt => opt.MapFrom(src => src.Name));
        CreateMap<User, UserModel>();
    }
}