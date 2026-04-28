namespace Diplom.Client.Models;

public class ClientOptions
{
    public string BaseUrl { get; set; } = "http://localhost:8000";
    public int TimeoutSeconds { get; set; } = 30;
    public string ExtractEmbeddingsEndpoint { get; set; } = "/extract-embeddings";
    public string HealthEndpoint { get; set; } = "/health";
    public int MaxFileSizeBytes { get; set; } = 10 * 1024 * 1024;
}