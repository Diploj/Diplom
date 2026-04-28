namespace Diplom.Data.Entities;

public class Group
{
    public int Id { get; set; }
    public int Number { get; set; }
    public int Year { get; set; }
    public DateTime CreateDate { get; set; } = DateTime.UtcNow;

    public ICollection<Student> Students { get; set; } = new List<Student>();
    public ICollection<Course> Courses { get; set; } = new List<Course>();
    public ICollection<Lecture> Lectures { get; set; } = new List<Lecture>();
}