namespace Diplom.BL.Filters;

public class LectureFilter
{
    public int? GroupId { get; set; }
    public int? CourseId { get; set; }
    public bool IsActual { get; set; } = false;
    public bool IsAttended { get; set; } = false;
}