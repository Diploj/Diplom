using Microsoft.AspNetCore.Identity;

namespace Diplom.Data.Entities;


public class User : IdentityUser<int>
{
    public string Name { get; set; }
    public string Surname { get; set; }
    public string Patronymic { get; set; }
}