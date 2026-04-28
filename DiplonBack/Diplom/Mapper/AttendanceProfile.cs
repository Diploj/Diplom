using AutoMapper;
using Diplom.BL.Models;
using Diplom.Controllers.Attendance.Contract;


namespace Diplom.Mapper;

public class AttendanceProfile : Profile
{
    public AttendanceProfile()
    {
        CreateMap<AttendanceDto, AttendanceModel>();
        CreateMap<AttendanceModel, AttendanceDto>();
    }
}