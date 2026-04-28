using System.Net.Mime;

namespace Diplom.BL.Services.Interface;

public interface IMinIOService
{
    Task<string> UploadStudentPhotoAsync(int faceEmbeddingId, Stream fileStream, string  contentType);
    Task<string> UploadLecturePhotoAsync(int lectureId, Stream fileStream, string  contentType);
     Task<(Stream Stream, string ContentType)> GetFileAsync(string bucket, string objectKey);
    Task DeleteFileAsync(string bucket, string objectKey);
}