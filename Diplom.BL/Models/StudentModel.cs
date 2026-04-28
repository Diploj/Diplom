namespace Diplom.BL.Models;

public class StudentModel
{
    public int? UserId { get; set; }
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
    public string Email { get; set; }
    public int? GroupId { get; set; }
    public string? StudentIdNumber { get; set; }
    public DateTime? EnrollmentDate { get; set; }
}