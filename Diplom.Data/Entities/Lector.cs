using System.ComponentModel.DataAnnotations;

namespace Diplom.Data.Entities;

public class Lector
{
    [Key]
    public int UserId { get; set; }
    public User User { get; set; } = null!;

    [MaxLength(255)]
    public string? Department { get; set; }
    
    public ICollection<Course> Courses { get; set; } = new List<Course>();
}