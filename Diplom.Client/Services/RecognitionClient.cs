using System.Net.Http.Json;
using System.Net.Http.Headers;
using System.Text.Json;
using Diplom.Client.Exceptions;
using Diplom.Client.Models;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace Diplom.Client.Services;

public class RecognitionClient : IRecognitionClient
{
    private readonly HttpClient _httpClient;
        private readonly ILogger<RecognitionClient> _logger;
        private readonly ClientOptions _options;

        public RecognitionClient(
            HttpClient httpClient,
            IOptions<ClientOptions> options,
            ILogger<RecognitionClient> logger)
        {
            _httpClient = httpClient;
            _logger = logger;
            _options = options.Value;
            
            _httpClient.BaseAddress = new Uri(_options.BaseUrl);
            _httpClient.Timeout = TimeSpan.FromSeconds(_options.TimeoutSeconds);
        }
        
        public async Task<List<float[]>> ExtractEmbeddingsAsync(Stream imageStream, CancellationToken cancellationToken = default)
        {
            if (imageStream == null)
                throw new ArgumentNullException(nameof(imageStream));
            
            if (imageStream.Length > _options.MaxFileSizeBytes)
            {
                throw new ClientException(
                    $"File size {imageStream.Length} bytes exceeds maximum allowed size of {_options.MaxFileSizeBytes} bytes");
            }

            try
            {
                _logger.LogInformation("Sending image to Python service for embedding extraction. Size: {FileSize} bytes", imageStream.Length);
                
                imageStream.Position = 0;
                
                using var formData = new MultipartFormDataContent();
                using var streamContent = new StreamContent(imageStream);
                
                streamContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
                
                formData.Add(streamContent, "file", "image.jpg");
                
                var response = await _httpClient.PostAsync(
                    _options.ExtractEmbeddingsEndpoint, 
                    formData, 
                    cancellationToken);
                
                if (!response.IsSuccessStatusCode)
                {
                    var errorContent = await response.Content.ReadAsStringAsync(cancellationToken);
                    _logger.LogError("Python service returned error {StatusCode}: {Error}", 
                        response.StatusCode, errorContent);
                    
                    throw new ClientException(
                        $"Python service returned error {response.StatusCode}",
                        (int)response.StatusCode,
                        errorContent);
                }
                
                var result = await response.Content.ReadFromJsonAsync<EmbeddingsResponse>(
                    cancellationToken: cancellationToken);

                if (result == null)
                {
                    throw new ClientException("Python service returned empty response");
                }

                _logger.LogInformation("Successfully extracted {Count} embeddings", result.Embeddings.Count);
                
                return result.Embeddings
                    .Select(e => e.ToArray())
                    .ToList();
            }
            catch (OperationCanceledException)
            {
                _logger.LogWarning("Python service request was cancelled");
                throw;
            }
            catch (HttpRequestException ex)
            {
                _logger.LogError(ex, "HTTP error occurred while calling Python service");
                throw new ClientException(
                    $"Failed to communicate with Python service: {ex.Message}", ex);
            }
            catch (JsonException ex)
            {
                _logger.LogError(ex, "Failed to deserialize response from Python service");
                throw new ClientException(
                    $"Invalid JSON response from Python service: {ex.Message}", ex);
            }
            catch (Exception ex) when (ex is not ClientException)
            {
                _logger.LogError(ex, "Unexpected error occurred while calling Python service");
                throw new ClientException(
                    $"Unexpected error: {ex.Message}", ex);
            }
        }
        
        public async Task<bool> HealthCheckAsync(CancellationToken cancellationToken = default)
        {
            try
            {
                var response = await _httpClient.GetAsync(_options.HealthEndpoint, cancellationToken);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Health check failed for Python service");
                return false;
            }
        }
    }

