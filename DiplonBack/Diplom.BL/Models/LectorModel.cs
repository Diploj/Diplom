namespace Diplom.BL.Models;

public class LectorModel
{
    public int? UserId { get; set; }
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
    public string Email { get; set; }
    public string? Department { get; set; }
}