using System.Linq.Expressions;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class StudentRepository : IRepository<Student>
{
    //protected readonly IDbContextFactory<AppDbContext> contextFactory;
    private readonly AppDbContext _context;

    public StudentRepository(AppDbContext context)
    {
        //this.contextFactory = contextFactory;
        this._context = context;
    }

    public async Task<Student?> GetByIdAsync(int id)
    {
        //await using var context = contextFactory.CreateDbContext();
        return await _context.Set<Student>().Include(s=> s.User).FirstOrDefaultAsync(s => s.UserId == id);
    }

    public async Task<IEnumerable<Student>> GetAllAsync(Expression<Func<Student, bool>> filter)
    {
        //await using var context = contextFactory.CreateDbContext();
        return await _context.Set<Student>()
            .Where(filter)
            .Include(s => s.User)
            .ToListAsync();
    }

    public async Task<Student> AddAsync(Student entity)
    {
        //await using var context = contextFactory.CreateDbContext();
        await _context.Set<Student>().AddAsync(entity);
        await _context.SaveChangesAsync();
        return entity;
    }
    
    public async Task AddRangeAsync(IEnumerable<Student> entities)
    {
        //await using var context = contextFactory.CreateDbContext();
        await _context.Set<Student>().AddRangeAsync(entities);
        await _context.SaveChangesAsync();;
    }

    public async Task UpdateAsync(Student entity)
    {
        //await using var context = contextFactory.CreateDbContext();
        _context.Set<Student>().Update(entity);
        await _context.SaveChangesAsync();
    }
    
    public async Task UpdateRangeAsync(IEnumerable<Student> entities)
    {
        //await using var context = contextFactory.CreateDbContext();
        _context.Set<Student>().UpdateRange(entities);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Student entity)
    {
        //await using var context = contextFactory.CreateDbContext();
        _context.Set<Student>().Remove(entity);
        await _context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        //await using var context = contextFactory.CreateDbContext();
        return await _context.Set<Student>().FindAsync(id) != null;
    }
}