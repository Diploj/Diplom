namespace Diplom.Controllers.User.Contract;

public class UserCreateRequest
{
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
    public string Email { get; set; }
    public string Password { get; set; }
}