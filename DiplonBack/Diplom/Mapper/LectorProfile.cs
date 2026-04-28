using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.User.Contract;

namespace Diplom.Mapper;

public class LectorProfile : Profile
{
    public LectorProfile()
    {
        CreateMap<LectorCreateRequest, LectorModel>();
        CreateMap<LectorModel, LectorDto>();
        CreateMap<LectorUpdateRequest, LectorModel>();
    }
}