using System.Linq.Expressions;
using Diplom.Data.Entities;

namespace Diplom.Data.Repository.Interface;

public interface IFaceEmbeddingRepository
{
    Task<FaceInfo?> GetByIdAsync(int id);
    Task<int> GetCount(int studentId);
    Task<IEnumerable<FaceInfo>> GetAllAsync(Expression<Func<FaceEmbedding, bool>> filter);
    Task<FaceEmbedding> AddAsync(FaceEmbedding entity);
    Task DeleteAsync(int id);
    Task<(int StudentId, float Distance)> GetNearestStudentAsync(float[] embeddings, int[] studentIds);
    Task<float> GetFurtherStudentsEmbeddingAsync(float[] embedding, int studentId);
}