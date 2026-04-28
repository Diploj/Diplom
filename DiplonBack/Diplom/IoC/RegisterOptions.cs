
using Diplom.BL.Options;
using Diplom.Client.Models;

namespace Diplom.IoC;

public class RegisterOptions
{
    public static void Register(IServiceCollection services, IConfiguration configuration)
    {
        services.Configure<ClientOptions>(
            configuration.GetSection("ClientOptions"));
        services.Configure<DistanceOptions>(
            configuration.GetSection("DistanceOptions"));
        services.Configure<JwtOptions>(
            configuration.GetSection("JwtOptions"));
        services.Configure<MinIOOptions>(
            configuration.GetSection("MinIOOptions"));
        services.Configure<PhotoOptions>(
            configuration.GetSection("PhotoOptions"));
    }
}

