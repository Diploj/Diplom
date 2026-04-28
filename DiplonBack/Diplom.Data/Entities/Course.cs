namespace Diplom.Data.Entities;

public class Course
{
    public int Id { get; set; }
    public int SubjectId { get; set; }
    public Subject Subject { get; set; } = null!;
    public int LectorId { get; set; }      
    public Lector Lector { get; set; } = null!;
    public DateTime StartDate { get; set; }
    public DateTime EndDate { get; set; }
    public ICollection<Group> Groups { get; set; } = new List<Group>();
    public ICollection<Lecture> Lectures { get; set; } = new List<Lecture>();
}