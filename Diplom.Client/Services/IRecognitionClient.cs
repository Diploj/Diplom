namespace Diplom.Client.Services;

public interface IRecognitionClient
{
    Task<List<float[]>> ExtractEmbeddingsAsync(Stream imageStream, CancellationToken cancellationToken = default);
    Task<bool> HealthCheckAsync(CancellationToken cancellationToken = default);
}