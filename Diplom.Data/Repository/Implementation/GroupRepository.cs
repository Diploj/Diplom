using System.Linq.Expressions;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class GroupRepository(AppDbContext context) : IRepository<Group>
{
    public async Task<Group?> GetByIdAsync(int id)
    {
        return await context.Set<Group>()
            .Include(g => g.Courses)
                .ThenInclude(c => c.Lector)
                    .ThenInclude(l => l.User)
            .Include(g => g.Courses)
                .ThenInclude(c => c.Subject)
            .Include(g => g.Lectures)
            .FirstOrDefaultAsync(c => c.Id == id);
    }

    public async Task<IEnumerable<Group>> GetAllAsync(Expression<Func<Group, bool>> filter)
    {
        return await context.Set<Group>()
            .Where(filter)            
            .Include(g => g.Courses)
            .Include(g => g.Lectures)
            .ToListAsync();
    }

    public async Task<Group> AddAsync(Group entity)
    {
        await context.Set<Group>().AddAsync(entity);
        await context.SaveChangesAsync();
        return entity;
    }
    
    public async Task AddRangeAsync(IEnumerable<Group> entities)
    {
        await context.Set<Group>().AddRangeAsync(entities);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Group entity)
    {
        context.Set<Group>().Update(entity);
        await context.SaveChangesAsync();
    }
    
    public async Task UpdateRangeAsync(IEnumerable<Group> entities)
    {
        context.Set<Group>().UpdateRange(entities);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Group entity)
    {
        context.Set<Group>().Remove(entity);
        await context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await context.Set<Group>().FindAsync(id) != null;
    }
}