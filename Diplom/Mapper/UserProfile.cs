using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.User.Contract;

namespace Diplom.Mapper;

public class UserProfile : Profile
{
    public UserProfile()
    {
        CreateMap<UserCreateRequest, UserModel>();
        CreateMap<UserModel, UserDto>();
    }
}