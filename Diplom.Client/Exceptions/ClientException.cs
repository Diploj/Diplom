namespace Diplom.Client.Exceptions;

public class ClientException : Exception
{
    public int? StatusCode { get; set; }
    public string? ResponseBody { get; set; }

    public ClientException(string message) : base(message) { }
        
    public ClientException(string message, Exception innerException) : base(message, innerException) { }
        
    public ClientException(string message, int statusCode, string responseBody) : base(message)
    {
        StatusCode = statusCode;
        ResponseBody = responseBody;
    }
}