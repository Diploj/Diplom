using System.Linq.Expressions;

namespace Diplom.Data.Repository.Interface;

public interface IRepository<T> where T : class
{
    Task<T?> GetByIdAsync(int id);
    Task<IEnumerable<T>> GetAllAsync(Expression<Func<T, bool>> filter);
    Task<T> AddAsync(T entity);
    Task AddRangeAsync(IEnumerable<T> entities);
    Task UpdateAsync(T entity);
    Task UpdateRangeAsync(IEnumerable<T> entities);
    Task DeleteAsync(T entity);
    Task<bool> ExistsAsync(int id);
}