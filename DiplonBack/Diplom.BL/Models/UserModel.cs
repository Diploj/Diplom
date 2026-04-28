namespace Diplom.BL.Models;

public class UserModel
{
    public int? Id { get; set; }
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
    public string Email { get; set; }
    public string? PasswordHash { get; set; }
}