using Diplom.BL.Mapper;
using Diplom.BL.Models;
using Diplom.Controllers.User.Contract;
using Diplom.Data.Entities;
using Diplom.Mapper;


namespace Diplom.IoC;

public class MapperConfigurator
{
    public static void ConfigureServices(IServiceCollection services)
    {

        services.AddAutoMapper(cfg =>
        {
            cfg.AddProfile<UserBLProfile>();
            cfg.AddProfile<AttendanceBLProfile>();
            cfg.AddProfile<GroupBLProfile>();
            cfg.AddProfile<CourseBLProfile>();
            cfg.AddProfile<LectorBLProfile>();
            cfg.AddProfile<LectureBLProfile>();
            cfg.AddProfile<StudentBLProfile>();
            cfg.AddProfile<SubjectBLProfile>();
            cfg.AddProfile<UserProfile>();
            cfg.AddProfile<StudentProfile>();
            cfg.AddProfile<LectorProfile>();
            cfg.AddProfile<SubjectProfile>();
            cfg.AddProfile<GroupProfile>();
            cfg.AddProfile<CourseProfile>();
            cfg.AddProfile<LectureProfile>();
            cfg.AddProfile<AttendanceProfile>();
        });
    }
}