namespace Diplom.Data.Entities;

public class Lecture
{
    public int Id { get; set; }
    public int CourseId { get; set; }
    public Course Course { get; set; } = null!;
    public DateTime Date { get; set; }
    public bool IsAttended {get; set;}

    public bool IsPhotoLoaded { get; set; } 
    public ICollection<Group> Groups { get; set; } = new List<Group>();
    public ICollection<Attendance> Attendances { get; set; } = new List<Attendance>();
}