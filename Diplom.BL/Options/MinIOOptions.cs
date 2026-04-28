namespace Diplom.BL.Options;

public class MinIOOptions
{
    public string Endpoint { get; set; } = "localhost:9000";
    public string AccessKey { get; set; } = "minioadmin";
    public string SecretKey { get; set; } = "minioadmin";
    public string BucketStudentsPhoto { get; set; } = "students";
    public string BucketLecturesPhoto { get; set; } = "lectures";
    public bool UseSSL { get; set; } = false;
    public int MaxFileSizeMB { get; set; } = 10;
}