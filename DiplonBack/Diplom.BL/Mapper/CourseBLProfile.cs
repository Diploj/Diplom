using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class CourseBLProfile : Profile
{
    public CourseBLProfile()
    {
        CreateMap<Course, CourseModel>()
            .ForMember(dest => dest.SubjectName,
                opt => opt.MapFrom(src => src.Subject.Name))
            .ForMember(dest => dest.LectorFullName,
                opt=> opt.MapFrom(src
                    => $"{src.Lector.User.Name} {src.Lector.User.Surname} {src.Lector.User.Patronymic}"));
        CreateMap<CourseModel, Course>();
    }
}