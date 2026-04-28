namespace Diplom.Controllers.Lecture.Contract;

public class LectureDto
{
    public int Id { get; set; }
    public int CourseId { get; set; }
    public bool IsPhotoLoaded { get; set; } 
    public bool IsAttended {get; set;}
    public DateTime Date { get; set; }
}