using System.ComponentModel.DataAnnotations;

namespace Diplom.Data.Entities;

public class Subject
{
    public int Id { get; set; }
    [Required]
    [MaxLength(255)]
    public string Name { get; set; }

    public ICollection<Course> Courses { get; set; } = new List<Course>();
}