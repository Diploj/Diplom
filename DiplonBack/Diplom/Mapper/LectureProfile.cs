using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.Lecture.Contract;

namespace Diplom.Mapper;

public class LectureProfile : Profile
{
    public LectureProfile()
    {
        CreateMap<LectureCreateRequest, LectureModel>();
        CreateMap<LectureModel, LectureDto>();
    }
}

