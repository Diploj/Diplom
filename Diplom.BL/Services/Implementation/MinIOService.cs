using System.Net.Mime;
using Diplom.BL.Options;
using Diplom.BL.Services.Interface;
using Microsoft.Extensions.Options;
using Serilog;
using Minio;
using Minio.DataModel.Args;

namespace Diplom.BL.Services.Implementation;

public class MinIOService : IMinIOService
{
    private readonly IMinioClient _minioClient;
    private readonly ILogger _logger;
    private readonly MinIOOptions _options;
    
    public MinIOService(IOptions<MinIOOptions> options, ILogger logger)
    {
        _options = options.Value;
        _logger = logger;
        _minioClient = new MinioClient()
            .WithEndpoint(_options.Endpoint)
            .WithCredentials(_options.AccessKey, _options.SecretKey)
            .WithSSL(_options.UseSSL)
            .Build();
    }
    
    private async Task EnsureBucketExists(string bucketName)
    {
        var found = await _minioClient.BucketExistsAsync(new BucketExistsArgs().WithBucket(bucketName));
        if (!found)
            await _minioClient.MakeBucketAsync(new MakeBucketArgs().WithBucket(bucketName));
    }
    
    public async Task<string> UploadStudentPhotoAsync(int studentId, Stream fileStream, string contentType)
    {
        await EnsureBucketExists(_options.BucketStudentsPhoto);
        var objectKey = $"{studentId}/{Guid.NewGuid()}";
        await _minioClient.PutObjectAsync(new PutObjectArgs()
            .WithBucket(_options.BucketStudentsPhoto)
            .WithObject(objectKey)
            .WithStreamData(fileStream)
            .WithObjectSize(fileStream.Length)
            .WithContentType(contentType));
        return objectKey;
    }

    public async Task<string> UploadLecturePhotoAsync(int lectureId, Stream fileStream, string contentType)
    {
        await EnsureBucketExists(_options.BucketLecturesPhoto);
        var objectKey = $"{lectureId}";
        await _minioClient.PutObjectAsync(new PutObjectArgs()
            .WithBucket(_options.BucketLecturesPhoto)
            .WithObject(objectKey)
            .WithStreamData(fileStream)
            .WithObjectSize(fileStream.Length)
            .WithContentType(contentType));
        return objectKey;
    }

    public async Task<(Stream Stream, string ContentType)> GetFileAsync(string bucket, string objectKey)
    {
        var memoryStream = new MemoryStream();
        try
        {
            var response = await _minioClient.GetObjectAsync(new GetObjectArgs()
                .WithBucket(bucket)
                .WithObject(objectKey)
                .WithCallbackStream(stream =>
                {
                    stream.CopyTo(memoryStream); // синхронное копирование
                }));
            memoryStream.Position = 0;
            return (memoryStream, response.ContentType);
        }
        catch (Exception)
        {
            await memoryStream.DisposeAsync();
            throw;
        }
    }

    public async Task DeleteFileAsync(string bucket, string objectKey)
    {
        await _minioClient.RemoveObjectAsync(new RemoveObjectArgs()
            .WithBucket(bucket)
            .WithObject(objectKey));
    }
}

