using System.ComponentModel.DataAnnotations;

namespace Diplom.Data.Entities;

public class Student
{
    [Key]
    public int UserId { get; set; }  
    public User User { get; set; } = null!;

    public int? GroupId { get; set; }
    public Group? Group { get; set; }

    [MaxLength(50)]
    public string? StudentIdNumber { get; set; }
    public DateTime? EnrollmentDate { get; set; }
    
    public ICollection<Attendance> Attendances { get; set; } = new List<Attendance>();
    public ICollection<FaceEmbedding> FaceEmbeddings { get; set; } = new List<FaceEmbedding>();
}