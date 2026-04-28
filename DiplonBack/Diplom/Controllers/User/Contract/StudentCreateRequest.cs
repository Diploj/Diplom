namespace Diplom.Controllers.User.Contract;

public class StudentCreateRequest
{
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
    public string Email { get; set; }
    public string Password { get; set; }
    public string? StudentIdNumber { get; set; }
    public DateTime? EnrollmentDate { get; set; }
}