using System.Linq.Expressions;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class Repository<T> : IRepository<T> where T : class
{
    private readonly AppDbContext _context;

    public Repository(AppDbContext context)
    {
        _context = context;
    }

    public async Task<T?> GetByIdAsync(int id)
    {
        return await _context.Set<T>().FindAsync(id);
    }

    public async Task<IEnumerable<T>> GetAllAsync(Expression<Func<T, bool>> filter)
    {
        return await _context.Set<T>()
            .Where(filter)
            .ToListAsync();
    }

    public async Task<T> AddAsync(T entity)
    {
        await _context.Set<T>().AddAsync(entity);
        await _context.SaveChangesAsync();
        return entity;
    }
    
    public async Task AddRangeAsync(IEnumerable<T> entities)
    {
        await _context.Set<T>().AddRangeAsync(entities);
        await _context.SaveChangesAsync();;
    }
    
    public async Task UpdateRangeAsync(IEnumerable<T> entities)
    {
        _context.Set<T>().UpdateRange(entities);
        await _context.SaveChangesAsync();
    }

    public async Task UpdateAsync(T entity)
    {
        _context.Set<T>().Update(entity);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(T entity)
    {
        _context.Set<T>().Remove(entity);
        await _context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await _context.Set<T>().FindAsync(id) != null;
    }
}