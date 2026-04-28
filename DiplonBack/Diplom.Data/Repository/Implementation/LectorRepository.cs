using System.Linq.Expressions;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class LectorRepository(AppDbContext context) : IRepository<Lector>
{
    public async Task<Lector?> GetByIdAsync(int id)
    {
        return await context.Set<Lector>()
            .Include(l=> l.User)
            .FirstOrDefaultAsync(l => l.UserId == id);
    }

    public async Task<IEnumerable<Lector>> GetAllAsync(Expression<Func<Lector, bool>> filter)
    {
        return await context.Set<Lector>()
            .Where(filter)
            .Include(l => l.User)
            .ToListAsync();
    }

    public async Task<Lector> AddAsync(Lector entity)
    {
        await context.Set<Lector>().AddAsync(entity);
        await context.SaveChangesAsync();
        return entity;
    }

    public async Task AddRangeAsync(IEnumerable<Lector> entities)
    {
        await context.Set<Lector>().AddRangeAsync(entities);
        await context.SaveChangesAsync();;
    }

    public async Task UpdateAsync(Lector entity)
    {
        context.Set<Lector>().Update(entity);
        await context.SaveChangesAsync();
    }
    public async Task UpdateRangeAsync(IEnumerable<Lector> entities)
    {
        context.Set<Lector>().UpdateRange(entities);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Lector entity)
    {
        context.Set<Lector>().Remove(entity);
        await context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await context.Set<Lector>().FindAsync(id) != null;
    }
}