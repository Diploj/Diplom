using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.Course;
using Diplom.Controllers.Course.Contract;

namespace Diplom.Mapper;

public class CourseProfile : Profile
{
    public CourseProfile()
    {
        CreateMap<CourseCreateRequest, CourseModel>();
        CreateMap<CourseModel, CourseDto>();
    }
}