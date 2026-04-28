namespace Diplom.Data.Entities;

public class Attendance
{
    public int Id { get; set; }
    public int LectureId { get; set; }
    public Lecture Lecture { get; set; } = null!;
    
    public int StudentId { get; set; }      
    public Student Student { get; set; } = null!;
    
    public bool Attended { get; set; }
}