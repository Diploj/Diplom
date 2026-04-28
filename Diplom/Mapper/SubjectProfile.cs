using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.Subject.Contract;
using Diplom.Data.Entities;

namespace Diplom.Mapper;

public class SubjectProfile : Profile
{
    public SubjectProfile()
    {
        CreateMap<SubjectCreateRequest, SubjectModel>();
        CreateMap<SubjectUpdateRequest, SubjectModel>();
        CreateMap<SubjectModel, SubjectDto>();
    }
}