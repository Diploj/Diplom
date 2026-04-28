using System.Linq.Expressions;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class LectureRepository : IRepository<Lecture>
{
    private readonly AppDbContext _context;
    public LectureRepository(AppDbContext context)
    {
        _context = context;
    }

    public async Task<Lecture?> GetByIdAsync(int id)
    {
        return await _context.Set<Lecture>()
            .Include(l=> l.Course)
            .Include(l=> l.Groups)
            .ThenInclude( g => g.Students)
            .Include(l=> l.Attendances)
            .FirstOrDefaultAsync( l => l.Id == id);
    }

    public async Task<IEnumerable<Lecture>> GetAllAsync(Expression<Func<Lecture, bool>> filter)
    {
        return await _context.Set<Lecture>()
            .Include(l=> l.Course)
            .Where(filter)
            .ToListAsync();
    }

    public async Task<Lecture> AddAsync(Lecture entity)
    {
        await _context.Set<Lecture>().AddAsync(entity);
        await _context.SaveChangesAsync();
        return entity;
    }
    public async Task AddRangeAsync(IEnumerable<Lecture> entities)
    {
        await _context.Set<Lecture>().AddRangeAsync(entities);
        await _context.SaveChangesAsync();;
    }

    public async Task UpdateAsync(Lecture entity)
    {
        _context.Set<Lecture>().Update(entity);
        await _context.SaveChangesAsync();
    }
    
    public async Task UpdateRangeAsync(IEnumerable<Lecture> entities)
    {
        _context.Set<Lecture>().UpdateRange(entities);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Lecture entity)
    {
        _context.Set<Lecture>().Remove(entity);
        await _context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await _context.Set<Lecture>().FindAsync(id) != null;
    }
}