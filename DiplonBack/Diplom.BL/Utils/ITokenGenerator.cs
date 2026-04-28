using System.IdentityModel.Tokens.Jwt;
using Diplom.BL.Models;

namespace Diplom.BL.Utils;

public interface ITokenGenerator
{
    JwtSecurityToken Generate(UserModel user, IList<string> roles);
}