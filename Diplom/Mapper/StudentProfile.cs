using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.User.Contract;

namespace Diplom.Mapper;

public class StudentProfile : Profile
{
    public StudentProfile()
    {
        CreateMap<StudentCreateRequest, StudentModel>();
        CreateMap<StudentUpdateRequest, StudentModel>();
        CreateMap<StudentModel, StudentDto>();
        CreateMap<FaceImageModel, FaceImageDto>();
    }
}