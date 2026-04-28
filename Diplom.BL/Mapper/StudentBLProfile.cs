using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class StudentBLProfile : Profile
{
    public StudentBLProfile()
    {
        CreateMap<Student, StudentModel>()
            .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.User.Email))
            .ForMember(dest => dest.Name, opt => opt.MapFrom(src => src.User.Name))
            .ForMember(dest => dest.Surname, opt => opt.MapFrom(src => src.User.Surname))
            .ForMember(dest => dest.Patronymic, opt => opt.MapFrom(src => src.User.Patronymic));
        CreateMap<StudentModel, Student>();
        CreateMap<StudentModel, User>()
            .ForMember(dest => dest.UserName, opt => opt.MapFrom(src => src.Name));
    }
}