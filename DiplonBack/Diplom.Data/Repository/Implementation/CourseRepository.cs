using System.Linq.Expressions;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class CourseRepository(AppDbContext context) : IRepository<Course>
{
    public async Task<Course?> GetByIdAsync(int id)
    {
        return await context.Set<Course>()
            .Include(c => c.Groups)
                .ThenInclude(g => g.Students)
                    .ThenInclude(s => s.User)
            .Include(c => c.Lector)
                .ThenInclude(l => l.User)
            .Include(c => c.Subject)
            .Include(c => c.Lectures)
            .FirstOrDefaultAsync(c => c.Id == id);
    }

    public async Task<IEnumerable<Course>> GetAllAsync(Expression<Func<Course, bool>> filter)
    {
        return await context.Set<Course>()
            .Where(filter)
            .Include(c => c.Groups)
            .Include(c => c.Lector)
                .ThenInclude(l => l.User)
            .Include(c => c.Subject)
            .ToListAsync();
    }

    public async Task<Course> AddAsync(Course entity)
    {
        await context.Set<Course>().AddAsync(entity);
        await context.SaveChangesAsync();
        return entity;
    }
    
    public async Task AddRangeAsync(IEnumerable<Course> entities)
    {
        await context.Set<Course>().AddRangeAsync(entities);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Course entity)
    {
        context.Set<Course>().Update(entity);
        await context.SaveChangesAsync();
    }
    
    public async Task UpdateRangeAsync(IEnumerable<Course> entities)
    {
        context.Set<Course>().UpdateRange(entities);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Course entity)
    {
        context.Set<Course>().Remove(entity);
        await context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await context.Set<Course>().FindAsync(id) != null;
    }
}