using Diplom.BL.Services.Implementation;
using Diplom.BL.Services.Interface;
using Diplom.BL.Utils;
using Diplom.Client.Models;
using Diplom.Client.Services;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Implementation;
using Diplom.Data.Repository.Interface;
using Microsoft.Extensions.Options;

namespace Diplom.IoC;

public class RegisterServices
{
    public static void Register(IServiceCollection services)
    {
        services.AddScoped<IRepository<Student>, StudentRepository>();
        services.AddScoped<IRepository<Lector>, LectorRepository>();
        services.AddScoped<IRepository<Lecture>, LectureRepository>();
        services.AddScoped<IRepository<Course>, CourseRepository>();
        services.AddScoped<IRepository<Group>, GroupRepository>();
        services.AddScoped<IFaceEmbeddingRepository, FaceEmbeddingRepository>();
        services.AddScoped(typeof(IRepository<>),typeof(Repository<>));
        services.AddHttpClient<IRecognitionClient, RecognitionClient>(
            (serviceProvider, client) =>
            {
                var options = serviceProvider.GetRequiredService<IOptions<ClientOptions>>().Value;
                client.BaseAddress = new Uri(options.BaseUrl);
                client.Timeout = TimeSpan.FromSeconds(options.TimeoutSeconds);
                client.DefaultRequestHeaders.Add("User-Agent", "CSharp-RecognitionService");
            });
        services.AddScoped<IUserService, UserService>();
        services.AddScoped<IStudentService, StudentService>();
        services.AddScoped<ILectorService, LectorService>();
        services.AddScoped<IAttendanceService, AttendanceService>();
        services.AddScoped<ICourseService, CourseService>();
        services.AddScoped<IGroupService, GroupService>();
        services.AddScoped<ILectureService, LectureService>();
        services.AddScoped<ISubjectService, SubjectService>();
        services.AddScoped<ITokenGenerator, TokenGenerator>();
        services.AddScoped<IMinIOService, MinIOService>();
    }
}