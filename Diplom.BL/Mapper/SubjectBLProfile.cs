using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class SubjectBLProfile : Profile
{
    public SubjectBLProfile()
    {
        CreateMap<SubjectModel, Subject>();
        CreateMap<Subject, SubjectModel>();
    }
}