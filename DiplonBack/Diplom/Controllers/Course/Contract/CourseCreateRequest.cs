namespace Diplom.Controllers.Course.Contract;

public class CourseCreateRequest
{
    public int SubjectId { get; set; }
    public int LectorId { get; set; }  
    public DateTime StartDate { get; set; }
    public DateTime EndDate { get; set; }
}