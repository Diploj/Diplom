using Diplom.Data;
using Microsoft.EntityFrameworkCore;

namespace Diplom.IoC;

public class DbContextConfigurator
{
    public static void ConfigureService(IServiceCollection services,string? connectionString)
    {
        services.AddDbContextFactory<AppDbContext>(
            options => { options.UseNpgsql(connectionString); },
            ServiceLifetime.Scoped);
        services.AddDbContext<AppDbContext>(
            options => { options.UseNpgsql(connectionString); },
            ServiceLifetime.Scoped
            );
    }

    public static void ConfigureApplication(IApplicationBuilder app)
    {
        using var scope = app.ApplicationServices.CreateScope();
        var contextFactory = scope.ServiceProvider.GetRequiredService<IDbContextFactory<AppDbContext>>();
        using var context = contextFactory.CreateDbContext();
        context.Database.Migrate();
    }
}