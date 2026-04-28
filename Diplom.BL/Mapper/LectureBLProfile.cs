using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class LectureBLProfile : Profile
{
    public LectureBLProfile()
    {
        CreateMap<Lecture, LectureModel>();
        CreateMap<LectureModel, Lecture>();
    }
}