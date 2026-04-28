using Diplom.Data.Entities;

namespace Diplom.BL.Models;

public class CourseModel
{
    public int? Id { get; set; }
    public int SubjectId { get; set; }
    public string SubjectName { get; set; } = "";
    public int LectorId { get; set; }
    public string LectorFullName { get; set; } = "";
    public DateTime StartDate { get; set; }
    public DateTime EndDate { get; set; }
}