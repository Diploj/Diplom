using AutoMapper;
using Diplom.BL.Models;
using Diplom.Data.Entities;

namespace Diplom.BL.Mapper;

public class AttendanceBLProfile : Profile
{
    public AttendanceBLProfile()
    {
        CreateMap<Attendance, AttendanceModel>();
        CreateMap<AttendanceModel, Attendance>();
    }
}