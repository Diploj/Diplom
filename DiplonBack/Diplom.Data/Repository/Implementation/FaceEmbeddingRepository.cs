using System.Linq.Expressions;
using Dapper;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.EntityFrameworkCore;

namespace Diplom.Data.Repository.Implementation;

public class FaceEmbeddingRepository(AppDbContext context) : IFaceEmbeddingRepository
{
    public async Task<FaceInfo?> GetByIdAsync(int id)
    {
        return await context.Set<FaceEmbedding>()
        .Select(fe => new FaceInfo
        {
            Id = fe.Id,
            StudentId = fe.StudentId,
            PhotoUrl = fe.PhotoUrl, 
            IsActive = fe.IsActive
        })
        .FirstOrDefaultAsync(c => c.Id == id);
    }

    public async Task<int> GetCount(int studentId)
    {
        return await context.Set<FaceEmbedding>()
            .Where(f => f.StudentId == studentId)
            .CountAsync();
    }

    public async Task<IEnumerable<FaceInfo>> GetAllAsync(Expression<Func<FaceEmbedding, bool>> filter)
    {
        return await context.Set<FaceEmbedding>()
            .Where(filter)
            .Select(fe => new FaceInfo
            {
                Id = fe.Id,
                StudentId = fe.StudentId,
                PhotoUrl = fe.PhotoUrl, 
                IsActive = fe.IsActive
            })
            .ToListAsync();
    }
    public async Task<FaceEmbedding> AddAsync(FaceEmbedding entity)
    {
        await context.Set<FaceEmbedding>().AddAsync(entity);
        await context.SaveChangesAsync();
        return entity;
    }

    public async Task DeleteAsync(int id)
    {
        var entity = new FaceEmbedding { Id = id };
        context.FaceEmbeddings.Attach(entity);
        context.FaceEmbeddings.Remove(entity);
        await context.SaveChangesAsync();
    }

    public async Task<bool> ExistsAsync(int id)
    {
        return await context.Set<FaceEmbedding>().FindAsync(id) != null;
    }

    public async Task<(int StudentId, float Distance)> GetNearestStudentAsync(float[] embedding, int[] studentIds)
    {
        var connection = context.Database.GetDbConnection();
        const string sql = @"
                SELECT fe.""StudentId"", (fe.""Embedding"" <=> @embedding::vector) AS Distance
                FROM ""FaceEmbeddings"" fe
                WHERE fe.""IsActive"" = true AND fe.""StudentId"" = ANY(@studentIds)
                ORDER BY Distance
                LIMIT 1;";

        var result = await connection.QueryFirstOrDefaultAsync<(int StudentId, float Distance)>(
            sql,
            new { embedding, studentIds });
        return result;
    }
    
    public async Task<float> GetFurtherStudentsEmbeddingAsync(float[] embedding, int studentId)
    {
        var connection = context.Database.GetDbConnection();
        const string sql = @"
                SELECT (fe.""Embedding"" <=> @embedding::vector) AS Distance
                FROM ""FaceEmbeddings"" fe
                WHERE fe.""IsActive"" = true AND fe.""StudentId"" = @studentId
                ORDER BY Distance DESC
                LIMIT 1;";

        var result = await connection.QueryFirstOrDefaultAsync<float>(
            sql,
            new { embedding, studentId });
        return result;
    }
}